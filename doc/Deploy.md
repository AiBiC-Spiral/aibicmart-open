# AiBiCmart Simulator デプロイ手順書

## サーバ側のRequirements

* JRE 8 以上
* Apache Tomcat 9.0 以上

## リソースの配備

`/opt/martresource` に以下のファイルを配備します。このディレクトリは学生から閲覧できないようにするべきです。

* 0NNNNNNNNNNNNN.csv : JANコードがNNNNNNNNNNNNのPOSデータ（シミュレートしたい商品数分用意する）
* weather.csv : 天候データ
* customer.csv : 来客数データ
* student.csv : 認証用ユーザ情報ファイル
* config.json : シミュレータ動作設定ファイル

各リソースの内容については [リソース仕様一覧](#リソース仕様一覧) を参照してください。

## ログ出力の準備

`/var/log/aibicmart` ディレクトリを作成し、tomcat が書き込み可能なように権限を設定してください。

## シミュレータの配備

tomcat の `webapp` ディレクトリに `aibicmart-open.war` を配備してください。

### 動作確認

`http://サーバー:tomcatのポート/aibicmart-open/api/gogo` にアクセスして、挨拶が返ってくれば動作しています。

これでシミュレータのデプロイは完了です。

## 独自ビルド

### ビルド手順

``` ./gradlew war ```

もしくはeclipseのプラグイン等を使用して "war" タスクを実行すると `build/libs/aibicmart-open.war` が生成されます。

### 主要な変更可能点

Windowsで動作させる場合やログのファイル名を変えたい場合など

* ログ出力の設定は `src/main/resources/log4j2.xml`

* リソースを配備するディレクトリの指定は`src/main/java/cloud/aibic_spiral/aibicmart/controller/ShopConfig.java`

## リソース仕様一覧

### 0NNNNNNNNNNNNN.csv : JANコードがNNNNNNNNNNNNのPOSデータ

| date| shopCode | JANCode | quantity | sales | productName | makerCode | makerName | JICFSCode | JICFSName |
-|-|-|-|-|-|-|-|-|-
|日付|（未使用）| *JANコード* | 販売数 | 売上金額 | *商品名* | *メーカーコード* | *メーカー名* | *JICFS分類コード* | *JICFS分類名* |

*強調表示*された項目は商品情報取得APIのみで使用するので1行目だけ記述すればよいフレーバーテキスト。

例：

```csv
date,shopCode,JANCode,quantity,sales,productName,makerCode,makerName,JICFSCode,JICFSName
2020-01-01,ABCM1,01234567123456,7,560,ヨーグルト 100g,1234567,PBL乳業,130205,ヨーグルト
...
```

### weather.csv : 天候データ

| date | Overview | rainfall | AverageTemp | MaxTemp | MinTemp | SolarRadiation |
-|-|-|-|-|-|-|-
| 日付 | 天気概要（雨、晴…など） | 降雨量 | 平均気温 | 最高気温 | 最低気温 | 日照時間 |

例：

```csv
date,Overview,rainfall,AverageTemp,MaxTemp,MinTemp,SolarRadiation
2020-01-01,晴,0,5.5,8.6,2.2,11.6
...
```

* 気象データは 気象庁 <http://www.data.jma.go.jp/gmd/risk/obsdl/> より取得
* 天候概況はそのままだと細かすぎる
  * 本PBLでは適当に簡素化して利用した

### customer.csv : 来客数データ

ヘッダー有。

| date | shopCode | numberOfCustomers |
--|--|--
| 日付 | （未使用） | 来客数 |

例：

```csv
date,shopCode,numberOfCustomers
2020-01-01,ABCM1,200
...
```

### student.csv : 認証用ユーザ情報ファイル

ヘッダー無。

| id | key |
--|--
|ユーザ名 |キー |

例：

```csv
testUser, test
...
```

* ユーザ名・キーともに文字列
* ユーザ名は店舗生成時のログに出力される
* キーは平文で扱うのでパスワードにしないほうがいい
  * 他人のIDで誤って店舗インスタンスを作成してしまうこと防ぐのが主な目的
  * 2文字程度でも十分
    * 現実的には、シミュレーションはそれほど時間がかからず終わるのでキーを探索して他人のIDで店舗を作成して妨害などはおそらく起きない
  * チームPBLの場合、チーム内では共有されることを想定する＝他のサービスでも使うようなパスワードにしない

### config.json : シミュレータ動作設定ファイル

| JSON Key | 型 | 概要 |
-|-|-
| simulate_start      | string | シミュレーション開始日下限 YYYY-MM-DD |
| simulate_end        | string | シミュレーション終了日上限 YYYY-MM-DD |
| stock_limit         | int | 在庫数上限 |
| product             | Array | 商品ごとの設定 |
| &emsp;JANCode       | string | JANコード |
| &emsp;default_price | int | 定価（販売価格の参考表示用） |
| &emsp;cost | int | 仕入れ価格 |
| &emsp;expire | int | 消費期限 |

例：

```json
{
    "simulate_start" : "2000-01-01",
    "simulate_end" : "2005-12-31",
    "stock_limit" : 10000,
    "product": [
        {
            "JANCode" : "01234567123456",
            "default_price" : 120,
            "cost"  : 55,
            "expire": 3
        }
    ]
}
```

## Tips

### Running on Debian

Debianのtomcatはsystemdによって隠蔽されているのでログが吐き出せない。
`/etc/systemd/system/multi-user.target.wants/tomcat9.service` を書き換えて `ReadWritePaths=/var/log/aibicmart/` を追加する必要あり。
