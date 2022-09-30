package com.ajl.xiaoan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MultiMonthView

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
        val tempColor = mSelectedPaint.color
        mSelectedPaint.color = (context as MainActivity).getSelectColor(calendar)
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
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
        mSelectedPaint.color = tempColor
        return false
    }

    override fun onDrawScheme(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        isSelected: Boolean
    ) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mExpectSolidPaint)
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2 -(mTextTipPaint.measureText("始")/2)
        val isInRange = isInRange(calendar)
        val isEnable = !onCalendarIntercept(calendar)
        if (isSelected) {
            if ((context as MainActivity).isStartDay(calendar)) {
                canvas.drawText("始", cx, baselineY, mTextTipPaint)
            } else if ((context as MainActivity).isEndDay(calendar)) {
                canvas.drawText("终", cx, baselineY, mTextTipPaint)
            }else{
                canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, mSelectTextPaint)
            }

        } else if (hasScheme) {
            canvas.drawText(
                calendar.day.toString(),
                cx.toFloat(),
                baselineY,
                if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth && isInRange && isEnable) mSchemeTextPaint else mOtherMonthTextPaint
            )
        } else {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), baselineY,
                if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth && isInRange && isEnable) mCurMonthTextPaint else mOtherMonthTextPaint
            )
        }
    }
}