# サンプルリソース

サンプルとして、2015/1/1～2016/12/31のシミュレートが可能なデータです。来客数・商品データは乱数や正規分布をもとに生成しています。祝祭日を考慮していないなど、現実に即していない部分があります。年毎にあまり傾向が変わらないので、予測するには簡単すぎるかもしれません。

## 商品データ

### 04912345000002.csv

1日50個売れます。価格変動もありません。

### 04912345000019.csv

気温に対して相関があります。あまりたくさん売れません。安い日と高い日があり、よく売れる日は必ず安い日です。

### 04912345000026.csv

曜日によって安売りされておりそのタイミングでよく売れます。そうでない日も年間を通して一定の売り上げがあります。

## そのほかのデータ

### weather.csv : 天候データ

2015/1/1～2016/12/31の東京の天候データを気象庁より取得し、編集したもの。

天候概況については4種類に簡略化しました。

```sh
sed -e 's/\(時々\|一時\|後\|、\)[^,]*,/,/g' | sed -e 's/快//g' -e 's/\(大雨\|暴風雨\|霧雨\)/雨/g' -e 's/薄曇,/曇,/g' -e 's/みぞれ,/雪,/g'
```

### customer.csv : 来客数データ

平均がおおよそ6000になるように一様乱数と正規分布を足し合わせたものです。

### student.csv : 認証用ユーザ情報ファイル

アカウント4人分を並べています。

```csv
teacher,enpit
2021001,a0
2021002,B9
2021003,c8

```

### config.json : シミュレータ動作設定ファイル

* シミュレーション可能な期間を2015-01-01～2015-12-31に限定
  * （学生が2015年のデータとシミュレータでモデルを作成し、教員がそのモデルを別のシミュレータを使って2016年で動かして成果を確認する、というような筋書きです）
* 在庫数上限は10000
* 上記の商品3種を全て取り扱う
  * 消費期限は3日

## Test run

curlを使って、簡単にシミュレータの動作を確認していきます。

```console
# curl 'http://localhost:8080/aibicmart-open/api/shops/?jan=04912345000002' -X POST -d '{"id":"teacher", "key":"enpit"}' -H "Content-Type:application/json"
{"token":77953,"since":"2015-01-01","until":"2015-12-31"}
```

店舗インスタンスが生成できました。期間指定をしていないのでシミュレート期間はconfig.jsonで指定された最大範囲です。tokenの値は以降のリクエストURLに含む必要があります。

```console
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/stock'
{"productStates":[],"total":0,"jancode":"04912345000002"}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/retailprice/relative/1'
{"date":"2015-01-01","price":300,"jancode":"04912345000002"}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/weather/relative/1'
{"date":"2015-01-01","overview":"曇","rainfall":"0","averageTemp":"3","maxTemp":"8","minTemp":"0","solarRadiation":"4"}
```

予測のために現在の在庫と、明日の販売価格・天気予報を取得しました。発注指示を待つシミュレータの状態は「その日のシミュレートが終わった夜」なので、予測したい日付は「明日」であることに注意してください。

```console
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/retailprice/of/2015-01-01'
{"date":"2015-01-01","price":300,"jancode":"04912345000002"}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/weather/of/2015-01-01'
{"date":"2015-01-01","overview":"曇","rainfall":"0","averageTemp":"3","maxTemp":"8","minTemp":"0","solarRadiation":"4"}
```

直接日付を指定する場合のサンプルです。

```console
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/order' -X POST -d '{"orders":[{"jancode":"04912345000002","amount":60}]}' -H "Content-Type:application/json"
{"date":"2014-12-31","status":[{"status":"success","amount":60,"jancode":"04912345000002"}]}# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/result'
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/result'
{"date":"2015-01-01","visitor":4332,"demand":50,"sold":50,"expired":0,"chanceloss":0,"jancode":"04912345000002","stock_open":60,"stock_close":10}
```

発注をして、その結果を確認しています。60個入荷したうち50個が売れました。

```console
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/order' -X POST -d '{"orders":[{"jancode":"04912345000002","amount":60}]}' -H "Content-Type:application/json"
{"date":"2015-01-01","status":[{"status":"success","amount":60,"jancode":"04912345000002"}]}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/order' -X POST -d '{"orders":[{"jancode":"04912345000002","amount":60}]}' -H "Content-Type:application/json"
{"date":"2015-01-02","status":[{"status":"success","amount":60,"jancode":"04912345000002"}]}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/order' -X POST -d '{"orders":[{"jancode":"04912345000002","amount":60}]}' -H "Content-Type:application/json"
{"date":"2015-01-03","status":[{"status":"success","amount":60,"jancode":"04912345000002"}]}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/order'
[{"date":"2015-01-01","orders":[{"amount":60,"jancode":"04912345000002"}]},{"date":"2015-01-02","orders":[{"amount":60,"jancode":"04912345000002"}]},{"date":"2015-01-03","orders":[{"amount":60,"jancode":"04912345000002"}]}]
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/stock'
{"productStates":[{"expiration":"2015-01-05","made":"2015-01-02","amount":10},{"expiration":"2015-01-06","made":"2015-01-03","amount":10}],"total":20,"jancode":"04912345000002"}
```

発注を繰り返してみます。発注の履歴はすべて記録されており、閲覧可能です。在庫を確認すると、消費期限別に売れ残っていることが見て取れます。

```console
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/summary'
{"uid":"teacher","since":"2015-01-01","until":"2015-12-31","jancode":"04912345000002","report_date":"2015-01-04","stock_last":20,"total_order":240,"total_sold":200,"total_expired":20,"total_chanceloss":0,"total_sales":60000,"total_cost":24000,"total_earnings":36000}
# curl 'http://localhost:8080/aibicmart-open/api/shops/77953/04912345000002/log'
date    visitor stockOpen       stockClose      demand  sold    expired chanceloss      orderAmount     sales   cost
2014-12-31      0       0       0       0       0       0       0       60      0       0
2015-01-01      4332    60      10      50      50      0       0       60      15000   6000
2015-01-02      5736    70      20      50      50      0       0       60      30000   12000
2015-01-03      5888    80      20      50      50      10      0       60      45000   18000
2015-01-04      5003    80      20      50      50      10      0       0       60000   24000
```

ここまでの結果をまとめて閲覧します。最終的な結果を閲覧するにはsummaryを、途中経過も含めた詳細な記録はlogを確認します。
