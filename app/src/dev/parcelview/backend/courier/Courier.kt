package dev.parcelview.backend.courier

enum class Courier(val value: String) {
    AMAZON("Amazon"),
    DHL("DHL"),
    FEDEX("FedEx"),
    ONTRAC("OnTrac"),
    UPS("UPS"),
    USPS("USPS"),
    UNKNOWN("Unknown"),
}