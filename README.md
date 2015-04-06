fujimiya_heroku
===========

> https://github.com/gecko655/fujimiyaBot を
> Google App Engine から heroku へ移植中。。。

> 以下、https://github.com/gecko655/fujimiyaBot からの引用


## 「わ、私... 月曜日には記憶がリセットされちゃうの...。」

Google App Engine上で動作するTwitter Botの制御プログラムです。
<https://twitter.com/fujimiya_monday> を動かしています。

### 主な機能

すべてのbot機能は、jp.gecko655.fujimiya.botの下のpackageにあります。

- fujimiyaBot
 - google画像検索で「藤宮さん」を検索し、ランダムに画像をツイート
- FujimiyaReply
 - フォロー外からのリプライを検知すると、リプライの送信主をフォローする
 - フォロー内からのリプライを検出すると、ランダムに画像をリプライする
- FujimiyaRemove
 - 月曜日の朝にフォロワーを全員リムーブし、友達のことを忘れる
- FujimiyaLunch
 - お昼になると、玉子焼きに関係する画像（？）をランダムにツイートする。



> なお、TwitterAPIのconsumerKeyなどを別ファイルで保存しているため、
このリポジトリをクローンしてもそのままでは動きません。

Special Thanks: https://twitter.com/nyoro_331/status/475542674838007808

なにかあれば[@gecko655](http://twitter.com/gecko655 "")まで。

