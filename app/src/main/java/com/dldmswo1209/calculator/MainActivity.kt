package com.dldmswo1209.calculator

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.dldmswo1209.calculator.databinding.ActivityMainBinding
import com.dldmswo1209.calculator.model.History
import java.lang.NumberFormatException
import kotlin.math.exp

class MainActivity : AppCompatActivity() {
    var mBinding : ActivityMainBinding? = null
    val binding get() = mBinding!!
    private var isOperator = false
    private var hasOperator = false
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()

    }
    fun buttonClicked(v: View){
        when(v.id){
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonMulti -> operatorButtonClicked("*")
            R.id.buttonDivider -> operatorButtonClicked("/")
            R.id.buttonModulo -> operatorButtonClicked("%")
        }
    }
    private fun numberButtonClicked(number: String){
        if (isOperator){ // ?????? ???????????? ????????????
            binding.expressiongTextView.append(" ") // ?????? ??????
        }
        isOperator = false

        val expressiontText = binding.expressiongTextView.text.split(" ")
        if(expressiontText.isNotEmpty() && expressiontText.last().length >= 15){
            Toast.makeText(this, "15?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
            return
        } else if (number == "0" && expressiontText.last().isEmpty()){
            Toast.makeText(this, "0??? ?????? ?????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show()
            return
        }
        binding.expressiongTextView.append(number)
        binding.resultTextView.text = calculateExpression() // ??????????????? ????????? ????????? ???????????? ???????????? ??????
    }
    private fun operatorButtonClicked(operator: String){
        if (binding.expressiongTextView.text.isEmpty())
            return
        when{
            isOperator -> { // ????????? ???????????? ?????? ??????(???????????? ??? ????????? ??????)
                val text = binding.expressiongTextView.text.toString()
                binding.expressiongTextView.text = text.dropLast(1) + operator
            }
            hasOperator -> { // ?????? ???????????? ?????? ??????
                Toast.makeText(this, "???????????? ??? ?????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                binding.expressiongTextView.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(binding.expressiongTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            binding.expressiongTextView.text.length-1,
            binding.expressiongTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.expressiongTextView.text = ssb

        isOperator = true
        hasOperator = true
    }
    private fun calculateExpression(): String{
        val expressionTexts = binding.expressiongTextView.text.split(" ")

        if (!hasOperator || expressionTexts.size != 3){ // ???????????? ????????? ???????????? ??? + ????????? ?????? 3??? ?????? ??????
            return ""
        }else if(!expressionTexts[0].isNumber() || !expressionTexts[2].isNumber()){
            return ""
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when(op){
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "*" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""
        }
    }

    fun clearButtonClicked(v: View){
        binding.expressiongTextView.text = ""
        binding.resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }
    fun resultButtonClicked(v: View){
        val expressionTexts = binding.expressiongTextView.text.split(" ")

        if (binding.expressiongTextView.text.isEmpty() || expressionTexts.size == 1){
            return
        }

        if (expressionTexts.size != 3 && hasOperator){
            Toast.makeText(this, "?????? ???????????? ?????? ?????? ?????????.", Toast.LENGTH_SHORT).show()
            return
        }

        if(!expressionTexts[0].isNumber() || !expressionTexts[2].isNumber()){
            Toast.makeText(this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
            return
        }
        val expressionText = binding.expressiongTextView.text.toString()
        val resultText = calculateExpression()

        // db??? ???????????? ??????
        // db??? ????????? ??? ??? ?????? ???????????? ?????? ????????? ??????????????? ?????? ???

        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        binding.resultTextView.text = ""
        binding.expressiongTextView.text = resultText

        isOperator = false
        hasOperator = false
    }

    fun historyButtonClicked(v: View){
        binding.historyLayout.isVisible = true
        binding.historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                runOnUiThread {
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "=${it.result}"

                    binding.historyLinearLayout.addView(historyView)
                }

            }
        }).start()
    }

    fun closeHistoryButtonClicked(v: View){
        binding.historyLayout.isVisible = false
    }

    fun historyClearButtonClicked(v: View){
        binding.historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()
        // ???????????? ?????? ?????? ??????
        // ?????? ?????? ?????? ????????????
    }

}

// String ?????? ??????
fun String.isNumber(): Boolean {
    return try{
        this.toBigInteger()
        true
    }catch (e: NumberFormatException){
        false
    }
}