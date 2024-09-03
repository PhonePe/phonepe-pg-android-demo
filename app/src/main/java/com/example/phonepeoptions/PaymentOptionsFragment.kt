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
        paymentOptionsViewModel.isLinkButtonVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.link.visibility = if (isVisible) {
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

        paymentOptionsViewModel.isProgressBarVisible.observe(viewLifecycleOwner) { isVisible
            binding.progressBar.visibility = if (isVisible) {
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
            Toast.makeText(requireContext(), ENTER_MERCHANT_ID, Toast.LENGTH_SHORT).show()
            return
        }
        val phonePeEnvironment = if (binding.environment.selectedItem == SANDBOX) {
            PhonePeEnvironment.SANDBOX
        } else {//environmentSpinner.selectedItem == PRODUCTION
            PhonePeEnvironment.RELEASE
        }

        val flowId = paymentOptionsViewModel.getRandomString(STRING_LENGTH)

        val result = PhonePeKt.init(
            requireContext(),
            binding.merchantId.text.toString(),
            flowId,
            phonePeEnvironment
        )

        if (result) {
            Toast.makeText(requireContext(), SDK_INIT_SUCCESSFUL, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), SDK_INIT_FAILED, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getInstrumentsClicked() {
        if (binding.savedInstrumentsToken.text.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                ENTER_SAVED_INSTRUMENTS_TOKEN,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            paymentOptionsViewModel.setIsProgressBarVisible(true)
            phonePeUserAccount.getUserInstruments(binding.savedInstrumentsToken.text.toString())
        }
    }

    private fun payClicked() {
        val selectedInstrument = paymentOptionsViewModel.selectedInstrument.value
        if (binding.orderToken.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), ENTER_ORDER_TOKEN, Toast.LENGTH_SHORT).show()
        } else if (selectedInstrument == null) {
            Toast.makeText(requireContext(), SELECT_A_PAYMENT_INSTRUMENT, Toast.LENGTH_SHORT)
                .show()
        } else if (!selectedInstrument.isAvailable) {
            Toast.makeText(
                requireContext(),
                THE_SELECTED_INSTRUMENT_IS_CURRENTLY_UNAVAILABLE,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            try {
                PhonePeKt.startTransaction(
                    activity = requireActivity(),
                    request = TransactionRequest(
                        orderId = paymentOptionsViewModel.getRandomString(STRING_LENGTH),
                        token = binding.orderToken.text.toString(),
                        paymentMode = getPaymentMode(
                            selectedInstrument.type,
                            selectedInstrument.accountId
                        )
                    ),
                    requestCode = MainActivity.REQUEST_CODE
                )
            } catch (ex: PhonePeInitException) {
                Toast.makeText(requireContext(), EXCEPTION  + ex, Toast.LENGTH_SHORT).show()
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
        phonePeUserAccount.onRestoreInstanceState(savedInstanceState)
    }

    //These are phonePeOptionsCallback
    override fun hideLinkButton(reason: LinkButtonHideReason) {
        paymentOptionsViewModel.setIsLinkButtonVisible(false)
    }

    override fun onConsentNotGiven() {
        Toast.makeText(requireContext(), CONSENT_NOT_GIVEN_BY_USER, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onInstrumentsReady(
        resultCode: InstrumentsResultCode,
        instruments: List<Instrument>?,
        additionalInfo: String?
    ) {
        paymentOptionsViewModel.setIsProgressBarVisible(false)

        if (resultCode != InstrumentsResultCode.SUCCESS) {
            Toast.makeText(
                requireContext(),
                INSTRUMENTS_RESULT_CODE + resultCode,
                Toast.LENGTH_SHORT
            ).show()
        } else if (instruments.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                NO_INSTRUMENTS_FOUND,
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
        paymentOptionsViewModel.setIsProgressBarVisible(false)
        paymentOptionsViewModel.setIsLinkButtonVisible(true)
    }

    companion object{
        private const val STRING_LENGTH = 20
        private const val SDK_INIT_SUCCESSFUL = "SDK init successful"
        private const val SDK_INIT_FAILED = "SDK init failed"
        private const val ENTER_SAVED_INSTRUMENTS_TOKEN = "Enter Saved Instruments Token"
        private const val ENTER_ORDER_TOKEN = "Enter Order Token"
        private const val SELECT_A_PAYMENT_INSTRUMENT = "Select a payment instrument"
        private const val THE_SELECTED_INSTRUMENT_IS_CURRENTLY_UNAVAILABLE = "The selected instrument is currently unavailable"
        private const val CONSENT_NOT_GIVEN_BY_USER = "Consent not given by user"
        private const val NO_INSTRUMENTS_FOUND = "No instruments found"
        private const val EXCEPTION = "Exception: "
        private const val INSTRUMENTS_RESULT_CODE = "InstrumentsResultCode: "
        private const val ENTER_MERCHANT_ID = "Enter Merchant Id"
        private const val SANDBOX = "SANDBOX"
    }
}