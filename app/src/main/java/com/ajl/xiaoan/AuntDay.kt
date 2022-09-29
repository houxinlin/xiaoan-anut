package com.ajl.xiaoan

import java.time.LocalDate


class AuntDay {
    var id: Long =0
    var startDay: LocalDate? =null
    var endDay: LocalDate ? =null

    constructor( startDay: LocalDate, endDay: LocalDate?) {
        this.startDay = startDay
        this.endDay = endDay
    }
    constructor()
}