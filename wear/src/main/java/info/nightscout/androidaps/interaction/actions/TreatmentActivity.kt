@file:Suppress("DEPRECATION")

package info.nightscout.androidaps.interaction.actions

import android.os.Bundle
import android.support.wearable.view.GridPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import info.nightscout.androidaps.R
import info.nightscout.androidaps.events.EventWearToMobile
import info.nightscout.androidaps.interaction.utils.PlusMinusEditText
import info.nightscout.shared.SafeParse.stringToDouble
import info.nightscout.shared.SafeParse.stringToInt
import info.nightscout.shared.weardata.EventData.ActionBolusPreCheck
import java.text.DecimalFormat
import kotlin.math.roundToInt

class TreatmentActivity : ViewSelectorActivity() {

    var editCarbs: PlusMinusEditText? = null
    var editInsulin: PlusMinusEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAdapter(MyGridViewPagerAdapter())
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    private inner class MyGridViewPagerAdapter : GridPagerAdapter() {

        override fun getColumnCount(arg0: Int): Int = 3
        override fun getRowCount(): Int = 1

        val incrementInsulin1 = (sp.getDouble(R.string.key_insulin_button_increment_1, 0.5) * 10).roundToInt() / 10.0
        val incrementInsulin2 = (sp.getDouble(R.string.key_insulin_button_increment_2, 1.0) * 10).roundToInt() / 10.0
        val incrementCarbs1 = sp.getInt(R.string.key_carbs_button_increment_1, 5).toDouble()
        val incrementCarbs2 = sp.getInt(R.string.key_carbs_button_increment_2, 10).toDouble()

        override fun instantiateItem(container: ViewGroup, row: Int, col: Int): Any {
            return if (col == 0) {
                val view = getInflatedPlusMinusView(container, true)
                var def = 0.0
                if (editInsulin != null) def = stringToDouble(editInsulin?.editText?.text.toString())
                val maxBolus = sp.getDouble(getString(R.string.key_treatments_safety_max_bolus), 3.0)
                val buttons = listOf(Pair(R.id.plusbutton, 0.1), Pair(R.id.plusbutton2, incrementInsulin1), Pair(R.id.plusbutton3, incrementInsulin2))
                editInsulin = PlusMinusEditText(view, R.id.amountfield, buttons, R.id.minusbutton, def, 0.0, maxBolus, 0.1, DecimalFormat("#0.0"), false)
                setLabelToPlusMinusView(view, getString(R.string.action_insulin))
                container.addView(view)
                view.requestFocus()
                view
            } else if (col == 1) {
                val view = getInflatedPlusMinusView(container, true)
                var def = 0.0
                val maxCarbs = sp.getInt(getString(R.string.key_treatments_safety_max_carbs), 48)
                if (editCarbs != null) def = stringToDouble(editCarbs?.editText?.text.toString())
                val buttons = listOf(Pair(R.id.plusbutton, 1.0), Pair(R.id.plusbutton2, incrementCarbs1), Pair(R.id.plusbutton3, incrementCarbs2))
                editCarbs = PlusMinusEditText(view, R.id.amountfield, buttons, R.id.minusbutton, def, 0.0, maxCarbs.toDouble(), 1.0, DecimalFormat("0"), false)
                setLabelToPlusMinusView(view, getString(R.string.action_carbs))
                container.addView(view)
                view
            } else {
                val view = LayoutInflater.from(applicationContext).inflate(R.layout.action_confirm_ok, container, false)
                val confirmButton = view.findViewById<ImageView>(R.id.confirmbutton)
                confirmButton.setOnClickListener {
                    //check if it can happen that the fragment is never created that hold data?
                    // (you have to swipe past them anyways - but still)
                    val bolus = ActionBolusPreCheck(stringToDouble(editInsulin?.editText?.text.toString()), stringToInt(editCarbs?.editText?.text.toString()))
                    rxBus.send(EventWearToMobile(bolus))
                    showToast(this@TreatmentActivity, R.string.action_treatment_confirmation)
                    finishAffinity()
                }
                container.addView(view)
                view
            }
        }

        override fun destroyItem(container: ViewGroup, row: Int, col: Int, view: Any) {
            // Handle this to get the data before the view is destroyed?
            // Object should still be kept by this, just setup for re-init?
            container.removeView(view as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`
    }
}
