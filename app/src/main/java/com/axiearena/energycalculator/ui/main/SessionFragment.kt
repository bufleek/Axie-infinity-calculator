package com.axiearena.energycalculator.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.axiearena.energycalculator.R
import com.axiearena.energycalculator.data.models.Session
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat

private const val ARG_SESSION = "session"

class SessionFragment : BottomSheetDialogFragment() {
    private var session: Session? = null
    private lateinit var tvSessionMode: TextView
    private lateinit var tvSessionBackupTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            session =
                Gson().fromJson(it.getString(ARG_SESSION), object : TypeToken<Session>() {}.type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSessionMode = view.findViewById(R.id.session_mode)
        tvSessionBackupTime = view.findViewById(R.id.session_last_backup)
        if (session == null) {
            dismiss()
        } else {
            session?.let {
                tvSessionMode.text =
                    if (it.isPcMode) "PC/DESKTOP MODE" else if (it.isBasicMode) "BASIC MODE" else "NORMAL MODE"

                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val dateString = simpleDateFormat.format(it.lastUpdated)
                tvSessionBackupTime.text = String.format("Last backup: %s", dateString)
            }
        }

        view.findViewById<MaterialCardView>(R.id.card_cancel_session).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialCardView>(R.id.card_restore_session).setOnClickListener {
            if (session!!.isPcMode) {
                startActivity(Intent(requireContext(), PcActivity::class.java).apply {
                    putExtra(PcActivity.INTENT_SESSION, Gson().toJson(session!!))
                })
                requireActivity().finish()
            } else {
                (requireActivity() as MainActivity).showFloatingWindow(
                    isBasicMode = session!!.isBasicMode,
                    session
                )
            }
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(session: String) =
            SessionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SESSION, session)
                }
            }
    }
}