package com.ajl.xiaoan

import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import com.ajl.xiaoan.gson.GsonUtils
import com.ajl.xiaoan.gson.LocalDateDeserializer
import com.ajl.xiaoan.gson.LocalDateSerializer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import okhttp3.*
import java.io.IOException
import java.text.MessageFormat
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue


class MainActivity : AppCompatActivity() {
    private lateinit var mCalendarView: CalendarView
    private lateinit var mTipTextView: TextView
    private lateinit var mMonthTextView: TextView
    private lateinit var listView:ListView
    private val okHttpClient = OkHttpClient.Builder().build()
    private lateinit var calendar: Calendar
    private var auntDayList: MutableList<AuntDay> = mutableListOf()
    private var dialogSelectCallback: () -> Unit = {}
    private lateinit var loadingProgressBar: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCalendarView = findViewById(R.id.calendarview)
        mTipTextView = findViewById(R.id.tv_tip)
        mMonthTextView = findViewById(R.id.tv_month)

        listView=findViewById(R.id.listview)
        loadingProgressBar = ProgressDialog(this)
        loadingProgressBar.setTitle("数据同步中")

        mMonthTextView.text = "${mCalendarView.curMonth}月"

        mCalendarView.setOnMonthChangeListener { year, month ->
            mMonthTextView.text = "${month}月"
        }
        mCalendarView.setOnCalendarMultiSelectListener(object :
            CalendarView.OnCalendarMultiSelectListener {
            override fun onCalendarMultiSelectOutOfRange(calendar: Calendar?) {
            }

            override fun onMultiSelectOutOfSize(calendar: Calendar?, maxSize: Int) {
            }

            override fun onCalendarMultiSelect(calendar: Calendar?, curSize: Int, maxSize: Int) {
                //如果单击后已被选中
                if (mCalendarView.multiSelectCalendars.contains(calendar)) {
                    this@MainActivity.calendar = calendar!!
                    showTypeSelectDialog()
                    return
                }
                //取消选中
                refresh(this@MainActivity.auntDayList)
            }
        })
        refreshFromServer()
    }

    fun getSelectColor(calendar: Calendar): Int {
        val targetDate = calendar.toLocalDate()
        for (auntDay in auntDayList) {
            if (auntDay.startDay!!.isEqual(targetDate) && auntDay.endDay == null) {
                return getColor(R.color.startColor)
            }
        }
        return getColor(R.color.finishColor)
    }

    fun isStartDay(calendar: Calendar): Boolean {
        val localDate = calendar.toLocalDate()
        return auntDayList.find { localDate.isEqual(it.startDay) } != null
    }

    fun isEndDay(calendar: Calendar): Boolean {
        val localDate = calendar.toLocalDate()
        return auntDayList.find { it.endDay != null && localDate.isEqual(it.endDay) } != null
    }

    private fun refreshFromServer() {
        sendGetRequest(Constant.LIST_URL, {
            refresh(GsonUtils.toList(it, object : TypeToken<MutableList<AuntDay?>?>() {}.type))
        })
    }

    private fun sendGetRequest(
        url: String,
        callback: OkHttpCallback,
        param: Map<String, String> = mutableMapOf()
    ) {
        loadingProgressBar.show()
        val newRequestUrl = StringBuffer()
        newRequestUrl.append(url)
        if (param.isNotEmpty()) {
            if (!newRequestUrl.endsWith("?")) {
                newRequestUrl.append("?")
            }
            param.forEach { (key, value) ->
                newRequestUrl.append("$key=$value&")
            }
        }
        log(newRequestUrl.toString())
        val request = Request.Builder()
            .url(newRequestUrl.toString())
        okHttpClient.newCall(request.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "网络请求失败", Toast.LENGTH_SHORT).show()
                    loadingProgressBar.hide()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                var string = response.body!!.string()

                runOnUiThread {
                    loadingProgressBar.hide()
                    callback.success(string)
                }
            }
        })
    }

    private fun refresh(data: MutableList<AuntDay>) {
        mCalendarView.removeMultiSelect(*mCalendarView.multiSelectCalendars.toTypedArray())
        data.forEach(this::addDay)
        auntDayList = data

        var transform: (AuntDay) -> HistoryBean = {
            HistoryBean().apply {
                this.startDate = it.startDay
                if (it.endDay!=null){
                    this.day =ChronoUnit.DAYS.between(it.startDay,it.endDay).toInt()+1
                }
            }
        }
        var list = auntDayList.map(transform)
        list= list.reversed()

        for(i in 0 until list.size-1){
            list[i].interval =ChronoUnit.DAYS.between(list[i].startDate,list[i+1].startDate).toInt().absoluteValue
        }
        listView.adapter =HistoryAdapter(list,this)
        //需不需要显示下一次生理期时间
        for (auntDay in auntDayList) {
            if (auntDay.endDay == null) return
        }

        getNextAuntDay()
    }

    private fun createLocalDate(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate.of(year, month, day)
    }

    private fun addDay(auntDay: AuntDay) {
        auntDay.endDay?.run {
            var tempDay = LocalDate.from(this)
            while (!tempDay.isEqual(auntDay.startDay!!.minusDays(1))) {
                mCalendarView.putMultiSelect(
                    getSchemeCalendar(
                        tempDay.year,
                        tempDay.monthValue,
                        tempDay.dayOfMonth,
                        Color.RED,
                        ""
                    )
                )
                tempDay = tempDay.minusDays(1)
            }
            return
        }
        mCalendarView.putMultiSelect(
            getSchemeCalendar(
                auntDay.startDay!!.year,
                auntDay.startDay!!.monthValue,
                auntDay.startDay!!.dayOfMonth,
                Color.RED,
                ""
            )
        )
    }

    /**
     * 添加开始时间
     */
    private fun addStartDay() {
        auntDayList.add(AuntDay(calendar.toLocalDate(), null))
        sendGetRequest(Constant.ADD_START_TIME, {
            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
            refreshFromServer()
        }, mutableMapOf("timer" to this.calendar.toStringDate()))
    }

    /**
     * 添加结束时间
     */
    private fun addEndDay() {
        //找到最后一个结束日期item
        this.auntDayList.reverse()
        for (auntDay in auntDayList) {
            if (auntDay.endDay == null) {
                if (this.calendar.toLocalDate().isBefore(auntDay.startDay)) {
                    Toast.makeText(this, "无法选择之前的时间", Toast.LENGTH_SHORT).show()
                    return
                }
                auntDay.endDay = this.calendar.toLocalDate()
            }
        }
        //本地刷新
        refresh(this.auntDayList)
        //远程刷新
        sendGetRequest(Constant.ADD_END_TIME, {
            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
            refreshFromServer()
        }, mutableMapOf("timer" to this.calendar.toStringDate()))

    }

    private fun getNextAuntDay() {
        if (auntDayList.isEmpty()) return
        val intervals = mutableListOf<Int>()
        var localDates = arrayListOf<LocalDate>()
        //保存所有开始时间
        for (auntDay in auntDayList) {
            localDates.add(auntDay.startDay!!)
        }

        //获取各个开始时间间隔
        for (i in 1 until auntDayList.size) {
            intervals.add(
                Period.between(
                    auntDayList[i - 1].startDay,
                    auntDayList[i].startDay,
                ).days
            )
        }

        log("sum=${intervals.sum()}")
        //获取平均值
        val avg = if (auntDayList.size == 1) 27 else intervals.average().toInt()

        //显示scheme
        val map: MutableMap<String, Calendar> = HashMap()
        var endDay = auntDayList.last().endDay!!.plusDays(avg.toLong())
        map[getSchemeCalendar(
            endDay.year,
            endDay.monthValue,
            endDay.dayOfMonth,
            Color.RED,
            "预期"
        ).toString()] =
            getSchemeCalendar(endDay.year, endDay.monthValue, endDay.dayOfMonth, Color.RED, "预期")

        mCalendarView.setSchemeDate(map)
        val nextDayInterval = ChronoUnit.DAYS.between(LocalDate.now(), endDay).absoluteValue

        mTipTextView.text = "距离下次生理期还剩${nextDayInterval}天！"

    }

    private fun dayTypeSelect(dialog: DialogInterface, index: Int) {
        if (index == 0) dialogSelectCallback.invoke()
    }

    /**
     * 显示选择 开始日期 还是 结束日期 对话框
     */
    private fun showTypeSelectDialog() {
        var startItems = arrayOf("开始日期", "取消")
        var endItems = arrayOf("结束日期", "取消")

        var items = startItems
        dialogSelectCallback = { addStartDay() }

        if (isNotFinish()) {
            items = endItems
            dialogSelectCallback = { addEndDay() }
        }

        AlertDialog.Builder(this)
            .setCancelable(false)
            .setOnDismissListener { refresh(this.auntDayList) }
            .setItems(items, this::dayTypeSelect).show()

    }

    /**
     * 是否需要一次结束日期
     */
    private fun isNotFinish(): Boolean {
        if (auntDayList.isEmpty()) return false
        for (auntDay in auntDayList) {
            if (auntDay.endDay == null) return true
        }
        return false
    }

    private fun log(msg: String) {
        Log.i("TAG", msg)
    }

    private fun getSchemeCalendar(
        year: Int,
        month: Int,
        day: Int,
        color: Int = Color.RED,
        text: String = ""
    ): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color //如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        return calendar
    }

    fun Int.fillZero(): String {
        return String.format("%02d", this)
    }

    fun Calendar.toLocalDate(): LocalDate {
        return LocalDate.of(this.year, this.month, this.day)
    }

    fun Calendar.toStringDate(): String {
        return "${this.year}-${this.month.fillZero()}-${this.day.fillZero()}"
    }


}