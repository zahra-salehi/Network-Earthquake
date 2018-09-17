package com.viranika.network

class Earthquake{

    var magnitude: Double
            private set

    var location: String
            private set

    var timeInMilliseconds: Long
            private set

    var url: String
            private set

    constructor(magnitude: Double, location: String, timeInMilliseconds: Long, url: String) {
                this.magnitude = magnitude
                this.location = location
                this.timeInMilliseconds = timeInMilliseconds
                this.url = url
        }
}

