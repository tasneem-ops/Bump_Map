package com.example.mapsdemo.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mapsdemo.databinding.FragmentLoginBinding
import com.example.mapsdemo.main_screen.MainActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {
    private lateinit var binding : FragmentLoginBinding
    private lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater)
        if (FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        binding.loginBtn.setOnClickListener {

            val email = binding.loginEmailEditText.text.toString()
            val password = binding.loginPasswordEditText.text.toString()
            val validData = checkEmailPasswordNotEmpty(email, password)
            if (validData){
                loginUser(email, password)
            }
            else{
                return@setOnClickListener
            }
        }

        binding.registerNow.setOnClickListener {
                this.findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }


    }
    private fun loginUser(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity(),
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")
                        val user = mAuth.currentUser
                        val mainActivityIntent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(mainActivityIntent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    private fun checkEmailPasswordNotEmpty(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please enter Email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Please enter Password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}