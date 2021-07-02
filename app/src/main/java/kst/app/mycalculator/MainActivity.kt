package kst.app.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.room.Room
import kst.app.mycalculator.model.History
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    private val expressionTv: TextView by lazy {
        findViewById<TextView>(R.id.expressionTv)
    }

    private val resultTv: TextView by lazy {
        findViewById<TextView>(R.id.resultTv)
    }

    private val historyLayout: ConstraintLayout by lazy {
        findViewById<ConstraintLayout>(R.id.historyLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }

    private var isOperator = false
    private var hasOperator = false

    lateinit var  db : AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()
    }

    fun buttonClicked(v: View){
        when(v.id){
            R.id.num0Bt -> numberBtClicked("0")
            R.id.num1Bt -> numberBtClicked("1")
            R.id.num2Bt -> numberBtClicked("2")
            R.id.num3Bt -> numberBtClicked("3")
            R.id.num4Bt -> numberBtClicked("4")
            R.id.num5Bt -> numberBtClicked("5")
            R.id.num6Bt -> numberBtClicked("6")
            R.id.num7Bt -> numberBtClicked("7")
            R.id.num8Bt -> numberBtClicked("8")
            R.id.num9Bt -> numberBtClicked("9")
            R.id.plusBt -> operatorButtonClicked("+")
            R.id.minusBt -> operatorButtonClicked("-")
            R.id.multiBt -> operatorButtonClicked("X")
            R.id.divisionBt -> operatorButtonClicked("/")
            R.id.percentBt -> operatorButtonClicked("%")
        }

    }

    private fun numberBtClicked(number: String){

        if(isOperator){
            expressionTv.append(" ")
        }

        isOperator = false

        val expresionText = expressionTv.text.split(" ")

        if (expresionText.isNotEmpty() && expresionText.last().length >= 15){
            Toast.makeText(this,"15자리 까지만 사용할 수 있습니다.",Toast.LENGTH_LONG).show()
            return
        } else if(number ==  "0" && expresionText.last().isEmpty()){
            Toast.makeText(this,"0은 제일 앞자리에 올 수 없습니다.",Toast.LENGTH_LONG).show()
            return
        }

        expressionTv.append(number)
        resultTv.text = calculateExpression()


    }

    private fun operatorButtonClicked(operator: String){
        if(expressionTv.text.isEmpty()){
            return
        }
        when{
            isOperator -> {
                val text = expressionTv.text.toString()
                expressionTv.text = text.dropLast(1) + operator
            }
            hasOperator -> {
                Toast.makeText(this,"연산자는 한번만 사용할 수 있습니다.",Toast.LENGTH_LONG).show()
                return
            }
            else -> {
                expressionTv.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(expressionTv.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.buttonGreen)),
            expressionTv.text.length-1,
            expressionTv.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        expressionTv.text = ssb

        isOperator = true
        hasOperator = true
    }

    fun resultButtonClicked(v: View){
        val expressionTexts =  expressionTv.text.split(" ")

        if (expressionTv.text.isEmpty() || expressionTexts.size == 1){
            return
        }

        if (expressionTexts.size != 3 && hasOperator){
            Toast.makeText(this,"아직 완성되지 않은 수식입니다.",Toast.LENGTH_LONG).show()
            return
        }

        if(expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()){
            Toast.makeText(this,"오류가 발생 했습니다.",Toast.LENGTH_LONG).show()
            return
        }

        val expressionText = expressionTv.text.toString()
        val resultText = calculateExpression()

        //DB Data 넣는 부분
        Thread(Runnable {
            db.historyDao().insertHistory(History(null,expressionText,resultText))
        }).start()

        resultTv.text = ""
        expressionTv.text = resultText

        isOperator = false
        hasOperator = false
    }

    fun clearButtonClicked(v: View){
        expressionTv.text = ""
        resultTv.text = ""
        isOperator = false
        hasOperator = false

    }

    fun historyButtonClicked(v: View){
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()
        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach{
                runOnUiThread {
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row,null,false)
                    historyView.findViewById<TextView>(R.id.expressionTv).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTv).text = "=${it.result}"
                    historyLinearLayout.addView(historyView)
                }
            }
        }).start()
    }

    fun closeHistoryBtClicked(v: View){
        historyLayout.isVisible = false
    }

    fun resetHistoryBtClicked(v: View){
        historyLinearLayout.removeAllViews()
        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()

    }

    private fun calculateExpression(): String{
        val expressionTexts =  expressionTv.text.split(" ")

        if (!hasOperator || expressionTexts.size !=3){
            return  ""
        } else if(expressionTexts[0].isNumber().not() ||
            expressionTexts[2].isNumber().not()){
            return ""
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]
        return when(op){
            "+" -> (exp1+exp2).toString()
            "-" -> (exp1-exp2).toString()
            "X" -> (exp1*exp2).toString()
            "/" -> (exp1/exp2).toString()
            "%" -> (exp1%exp2).toString()
            else -> ""
        }
    }
}

fun String.isNumber(): Boolean{
    return try{
        this.toBigInteger()
        true
    } catch (e: NumberFormatException){
        false
    }
}