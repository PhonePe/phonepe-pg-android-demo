package com.example.phonepeoptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phonepeoptions.adapter.SavedInstrumentsAdapter
import com.example.phonepeoptions.databinding.PaymentOptionsFragmentBinding
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.PhonePeKt
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.phonepe.intent.sdk.api.models.transaction.TransactionRequest
import com.phonepe.intent.sdk.api.ppeInstruments.PhonePeUserAccount
import com.phonepe.intent.sdk.api.ppeInstruments.contract.PhonePeUserAccountProvider
import com.phonepe.intent.sdk.api.ppeInstruments.helpers.PhonePeInstrumentHelper.getPaymentMode
import com.phonepe.intent.sdk.api.ppeInstruments.models.Instrument
import com.phonepe.intent.sdk.api.ppeInstruments.models.InstrumentsResultCode
import com.phonepe.intent.sdk.api.ppeInstruments.models.LinkButtonHideReason

class PaymentOptionsFragment : Fragment(), PhonePeUserAccountProvider {
    private lateinit var binding: PaymentOptionsFragmentBinding
    private val paymentOptionsViewModel: PaymentOptionsViewModel by viewModels()
    private lateinit var savedInstrumentsAdapter: SavedInstrumentsAdapter

    private lateinit var phonePeUserAccount: PhonePeUserAccount

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PaymentOptionsFragmentBinding.inflate(inflater, container, false)

        binding.savedInstrumentsRecyclerView.layoutManager = LinearLayoutManager(context)

        savedInstrumentsAdapter = SavedInstrumentsAdapter {
            paymentOptionsViewModel.setSelectedInstrument(it)
        }

        binding.savedInstrumentsRecyclerView.adapter = savedInstrumentsAdapter

        setOnClickListeners()
        setObservers()

        phonePeUserAccount = PhonePeUserAccount(this)

        return binding.root
    }

    private fun setObservers() {
        paymentOptionsViewModel.showLinkButton.observe(viewLifecycleOwner) {
            binding.link.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        paymentOptionsViewModel.savedInstrumentsList.observe(viewLifecycleOwner) {
            savedInstrumentsAdapter.updateList(it)
        }

        paymentOptionsViewModel.selectedInstrument.observe(viewLifecycleOwner) {
            savedInstrumentsAdapter.setSelectedInstrument(it)
        }

        paymentOptionsViewModel.showProgressBar.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setOnClickListeners() {
        binding.initSdk.setOnClickListener {
            initSDKClicked()
        }
        binding.getInstruments.setOnClickListener {
            getInstrumentsClicked()
        }
        binding.pay.setOnClickListener {
            payClicked()
        }
        binding.link.setOnClickListener {
            linkClicked()
        }
    }

    private fun initSDKClicked() {
        if (binding.merchantId.text.toString().isEmpty()) {
            Toast.makeText(this.requireContext(), "Enter Merchant Id", Toast.LENGTH_SHORT).show()
            return
        }
        val phonePeEnvironment = if (binding.environment.selectedItem == "SANDBOX") {
            PhonePeEnvironment.SANDBOX
        } else {//environmentSpinner.selectedItem == "PRODUCTION"
            PhonePeEnvironment.RELEASE
        }

        val flowId = paymentOptionsViewModel.getRandomString(STRING_LENGTH)

        val result = PhonePeKt.init(
            this.requireContext(),
            binding.merchantId.text.toString(),
            flowId,
            phonePeEnvironment
        )

        if (result) {
            Toast.makeText(this.requireContext(), "SDK init successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this.requireContext(), "SDK init failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getInstrumentsClicked() {
        if (binding.savedInstrumentsToken.text.toString().isEmpty()) {
            Toast.makeText(
                this.requireContext(),
                "Enter Saved Instruments Token",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            paymentOptionsViewModel.setShowProgressBar(true)
            phonePeUserAccount.getUserInstruments(binding.savedInstrumentsToken.text.toString())
        }
    }

    private fun payClicked() {
        if (binding.orderToken.text.toString().isEmpty()) {
            Toast.makeText(this.requireContext(), "Enter Order Token", Toast.LENGTH_SHORT).show()
        } else if (paymentOptionsViewModel.selectedInstrument.value == null) {
            Toast.makeText(this.requireContext(), "Select a payment instrument", Toast.LENGTH_SHORT)
                .show()
        } else if (paymentOptionsViewModel.selectedInstrument.value?.isAvailable == false) {
            Toast.makeText(
                this.requireContext(),
                "The selected instrument is currently unavailable",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            try {
                paymentOptionsViewModel.selectedInstrument.value?.let { value ->
                    PhonePeKt.startTransaction(
                        activity = requireActivity(),
                        request = TransactionRequest(
                            orderId = paymentOptionsViewModel.getRandomString(STRING_LENGTH),
                            token = binding.orderToken.text.toString(),
                            paymentMode = getPaymentMode(
                                value.type,
                                value.accountId
                            )
                        ),
                        requestCode = MainActivity.REQUEST_CODE
                    )
                }
            } catch (ex: PhonePeInitException) {
                Toast.makeText(this.requireContext(), "Exception: $ex", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun linkClicked() {
        phonePeUserAccount.linkPhonePe()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        phonePeUserAccount.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            phonePeUserAccount.onRestoreInstanceState(savedInstanceState)
        }
    }

    //These are phonePeOptionsCallback
    override fun hideLinkButton(reason: LinkButtonHideReason) {
        paymentOptionsViewModel.setShowLinkButton(false)
    }

    override fun onConsentNotGiven() {
        Toast.makeText(this.requireContext(), "Consent not given by user", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onInstrumentsReady(
        resultCode: InstrumentsResultCode,
        instruments: List<Instrument>?,
        additionalInfo: String?
    ) {
        paymentOptionsViewModel.setShowProgressBar(false)

        if (resultCode != InstrumentsResultCode.SUCCESS) {
            Toast.makeText(
                this.requireContext(),
                "InstrumentsResultCode: $resultCode",
                Toast.LENGTH_SHORT
            ).show()
        } else if (instruments.isNullOrEmpty()) {
            Toast.makeText(
                this.requireContext(),
                "No instruments found",
                Toast.LENGTH_SHORT
            ).show()
        }
        paymentOptionsViewModel.setSelectedInstrument(null)
        paymentOptionsViewModel.updateSavedInstrumentsList(
            paymentOptionsViewModel.convertPaymentOptionsListToSavedInstrumentsList(
                instruments
            )
        )
    }

    override fun showLinkButton() {
        paymentOptionsViewModel.setShowProgressBar(false)
        paymentOptionsViewModel.setShowLinkButton(true)
    }

    companion object{
        private const val STRING_LENGTH = 20
    }
}