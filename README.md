# COVID19 Case Tracker App & AppWidget 

#### Uygulama, en güncel COVID19 verilerine hızlı ve kolay bir biçimde ulaşmanızı sağlamaktadır. Bunun yanında AppWidget desteği sayesinde de Anasayfanıza ekleyeceğiniz Widget ile uygulamaya girmeksizin güncel verileri görebilirsiniz.

Widget:
- 3 Saatte bir otomatik olarak verileri güncellemektedir

Bunu değiştirmek için `res > xml > covid_widget_info.xml` yoluna ulaşarak `android:updatePeriodMillis` değerini milisaniye cinsinden arttırabilir veya azaltabilirsiniz.

Örnek:
```xml
    android:updatePeriodMillis="14400000"
```

*`Güncelleme periyodu minimum 30dk'ya düşürülebilir`*


## Veri Kaynakları
- [TC. Sağlık Bakanlığı](https://covid19.saglik.gov.tr)
- [Wikipedia](https://en.m.wikipedia.org/wiki/COVID-19_pandemic_by_country_and_territory)

## Kullanılan Kütüphaneler
-   [OkHttp](https://square.github.io/okhttp/)
-   [Gson](https://github.com/google/gson)
-   [FlowingDrawer](https://github.com/mxn21/FlowingDrawer)
-   [Android-SpinKit](https://github.com/ybq/Android-SpinKit)
-   [Material Components](https://github.com/material-components/material-components-android)
-   [Recyclerview](https://developer.android.com/jetpack/androidx/releases/recyclerview)

## İkon Paketi
- [Flaticon](https://www.flaticon.com/)

# Screenshot
<img src="https://github.com/ahmetgezici/COVID19-Case-Tracker-App/blob/master/Screenshots/covidapp1.png" width=40% /> <img src="https://github.com/ahmetgezici/COVID19-Case-Tracker-App/blob/master/Screenshots/covidapp2.png" width=40% />

# Widget
<img src="https://github.com/ahmetgezici/COVID19-Case-Tracker-App/blob/master/Screenshots/covidapp3.png" width=40% />
