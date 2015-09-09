fujimiya
===========

## 「わ、私... 月曜日には記憶がリセットされちゃうの...。」

Heroku及びDocker上で動作するTwitter Botの制御プログラムです。
<https://twitter.com/fujimiya_monday> を動かしています。

### 主な機能

すべてのbot機能は、jp.gecko655.fujimiya.botの下のpackageにあります。

- fujimiyaBot
 - google画像検索で「藤宮さん」を検索し、ランダムに画像をツイート
- FujimiyaReply
 - フォロー外からリプライを受け取ると、リプライの送信主をフォローする
 - フォロー内からリプライを受け取ると、google画像検索で「藤宮さん」を検索し、ランダムに画像をリプライする
 - フォロー内から「違う」などのリプライを受け取ると、リプライ先のツイートを削除して、そのツイートに含まれる画像を今後つぶやかないようにする
- FujimiyaRemove
 - 月曜日の朝にフォロワーを全員リムーブし、友達のことを忘れる
- FujimiyaLunch
 - お昼になると、玉子焼きに関係する画像（？）をランダムにツイートする。

Special Thanks: https://twitter.com/nyoro_331/status/475542674838007808

なにかあれば[@gecko655](http://twitter.com/gecko655 "")まで。

