package ru.geekbrains.lesson9_kotlin_pick_contact_and_call

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.geekbrains.lesson9_kotlin_pick_contact_and_call.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var number: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdvanced.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.container,ContactsAdvancedFragment.newInstance()).commit()
        }
        binding.btnBeginner.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.container,ContactsBeginnersFragment.newInstance()).commit()
        }
    }
}