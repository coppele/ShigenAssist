# ShigenAssist
ランク情報や、採掘数が知れたり、果ては空を飛んだり暗視ができたりします٩( 'ω' )و

## コマンド / Command
`/sa` - スコアボードの表示設定を開きます。</br>
`/sa help` - ShigenAssistで実行できるコマンドを確認します。</br>
`/sa reload` - ymlを再読み込みします。</br>
`/sa load` - ymlを読み込みします。</br>
`/sa save` - ymlに保存します。</br>
`/sa <on|enable> <assist|notice|night_vision|elytra|remarks>` - いずれかを起動します</br>
`/sa <off|disable> <assist|notice|night_vision|elytra|remarks>` - いずれかを停止します</br>
`/sa switch <assist|notice|night_vision|elytra|remarks>` - いずれかを起動または停止します</br>
`/sa remarks add [text]` - 備考に新しいテキストを追加します。</br>
`/sa remarks remove [text]` - 備考のテキストを削除します。</br>
`/sa remarks next` - 次の備考に移動します。</br>
`/sa remarks list` - 備考をまとめて確認します。</br>

`/sa elytra | /ee` - エフェクトの設定画面を開きます。

## 権限 / Permission
```Yaml
permissions:
  shigenassist.*:
    description: ShigenAssistの全ての権限です。
    default: true
    children:
      - shigenassist.command
      - shigenassist.assist.*
  shigenassist.command:
    description: ShigenAssistのコマンドの権限です。
    default: true
  shigenassist.assist.*:
    description: ShigenAssistの補助関連の権限です。
    default: true
    children:
      - shigenassist.assist.notice
      - shigenassist.assist.night_vision
      - shigenassist.assist.elytra
      - shigenassist.assist.remarks
  shigenassist.assist.notice:
    description: ShigenAssistの破壊警告の使用権限です。
    default: true
  shigenassist.assist.night_vision:
    description: ShigenAssistの暗視の使用権限です。
    default: true
  shigenassist.assist.elytra:
    description: ShigenAssistのエリトラ補助の使用権限です。
    default: true
  shigenassist.assist.remarks:
    description: ShigenAssistの備考欄の使用権限です。
    default: true
```

## 現在確認できている問題
- エリトラブーストのチャージをする際に表示されるゲージにずれがある問題

(`2022/02/07`現在確認されている問題は上記のものです)

## 修正済み問題
- 二度目のログイン以降にコマンドを実行するとエラーが発生する問題
- ワールド移動時にスコアボードの描写が更新されなくなる問題
- GUIの画面内にアイテムが投入できてしまう問題
- スコアボード上の`次のランクまで`の数値の問題
- スコアボードがプレイヤー間で同期されている問題
- GUI、スコアボードにて本来なら`有効/無効`となる表示のものが`表示/非表示`になっている問題
- Destroyer+がサーバーにログインするとスコアボードが停止する問題

## 修正できていないかもしれない問題
- サーバー移動後に暗視の効果が切れない問題?

## リリースノート
**1.0.0 - リリース**\
1.0.1 - 問題の修正とエリトラの機能追加...\
1.0.2 - 問題の修正...\
1.0.3 - 権限名を修正...\
1.0.4 - 問題と権限名の修正...\
1.0.5 - 表記ミスの修正...\
1.0.6 - スコアボードの問題の修正...\
1.0.7 - 前記の所をさらに修正...