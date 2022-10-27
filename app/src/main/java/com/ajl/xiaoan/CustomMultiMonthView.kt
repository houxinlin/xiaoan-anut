package com.ajl.xiaoan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MultiMonthView
import java.time.LocalDate
import kotlin.math.log

class CustomMultiMonthView(context: Context?) : MultiMonthView(context) {
    private var mRadius = 0
    private val mExpectPaint = Paint()
    private val mExpectSolidPaint = Paint()
    private val mTextTipPaint = Paint()

    init {
        mExpectPaint.textSize = 22f
        mExpectSolidPaint.color = getContext().getColor(R.color.expectColor)
        mExpectPaint.color = Color.WHITE
        mExpectSolidPaint.style = Paint.Style.FILL
        mTextTipPaint.color = getContext().getColor(R.color.textTipColor)
        mTextTipPaint.textSize = 40f

    }

    override fun onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.STROKE
    }

    override fun onDrawSelected(
        canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean,
        isSelectedPre: Boolean, isSelectedNext: Boolean
    ): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        mSelectedPaint.color= context.getColor(R.color.selectColor)

        if (isSelectedPre) {
            if (isSelectedNext) {
                canvas.drawRect(
                    x.toFloat(),
                    (cy - mRadius).toFloat(),
                    (x + mItemWidth).toFloat(),
                    (cy + mRadius).toFloat(),
                    mSelectedPaint
                )
            } else { //最后一个，the last
                canvas.drawRect(
                    x.toFloat(),
                    (cy - mRadius).toFloat(),
                    cx.toFloat(),
                    (cy + mRadius).toFloat(),
                    mSelectedPaint
                )
                canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
            }
        } else {
            if (isSelectedNext) {
                canvas.drawRect(
                    cx.toFloat(),
                    (cy - mRadius).toFloat(),
                    (x + mItemWidth).toFloat(),
                    (cy + mRadius).toFloat(),
                    mSelectedPaint
                )
            }
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        }
        return false
    }

    override fun onDrawScheme(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        isSelected: Boolean
    ) {

        Log.i("TAG", "onDrawScheme: $calendar")
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mExpectSolidPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        var cx = (x + mItemWidth / 2).toFloat()
        val isInRange = isInRange(calendar)
        val isEnable = !onCalendarIntercept(calendar)
        if (isSelected) {
            cx -= (mTextTipPaint.measureText("始") / 2)
            if ((context as MainActivity).isStartDay(calendar)) {
                canvas.drawText("始", cx, baselineY, mTextTipPaint)
            } else if ((context as MainActivity).isEndDay(calendar)) {
                canvas.drawText("终", cx, baselineY, mTextTipPaint)
            } else {
                canvas.drawText(calendar.day.toString(), cx, baselineY, mSelectTextPaint)
            }

        } else if (hasScheme) {
            val scheme = (context as MainActivity).getScheme()
            val schemeCalendar = scheme[calendar.toString()]!!
            val paint = if (calendar.isCurrentDay) mCurDayTextPaint
            else if (calendar.isCurrentMonth && isInRange && isEnable) mSchemeTextPaint
            else mOtherMonthTextPaint
            paint.color = Color.WHITE
            canvas.drawText(schemeCalendar.scheme, cx, baselineY, paint)
        } else if (calendar.isCurrentDay) {
            Log.i("TAG", "onDrawText: 今天")
            canvas.drawText("今", cx, baselineY, mCurDayTextPaint)
        } else {
            val paint = if (calendar.isCurrentDay) mCurDayTextPaint
            else if (calendar.isCurrentMonth && isInRange && isEnable) mCurMonthTextPaint
            else mOtherMonthTextPaint
            canvas.drawText(calendar.day.toString(), cx, baselineY, paint)
        }
    }

}