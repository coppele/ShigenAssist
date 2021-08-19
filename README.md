# ShigenAssist
ランク情報や、採掘数、果ては空を飛んだり暗視ができたりします٩( 'ω' )و

## コマンド / Command
`/sa` - スコアボードの表示設定を開きます。</br>
`/sa help` - ShigenAssistで実行できるコマンドを確認します。</br>
`/sa reload` - ymlを再読み込みします。</br>
`/sa load` - ymlを読み込みします。</br>
`/sa save` - ymlに保存します。</br>
`/sa remarks add [text]` - 備考に新しいテキストを追加します。</br>
`/sa remarks remove [text]` - 備考のテキストを削除します。</br>
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
  shigenassist.assist.notice:
    description: ShigenAssistの破壊警告の使用権限です。
    default: true
  shigenassist.assist.night_vision:
    description: ShigenAssistの暗視の使用権限です。
    default: true
  shigenassist.assist.elytra:
    description: ShigenAssistのエリトラ補助の使用権限です。
    default: true
  shigenassist.assist.remarks.show:
    description: ShigenAssistの備考欄の表示権限です。
    default: true
  shigenassist.assist.remarks.edit:
    description: ShigenAssistの備考欄の編集権限です。
    default: true
```

## 現在確認できている問題
2021/08/19 現在確認されていません

## 修正済み問題
- 二度目のログイン以降にコマンドを実行するとエラーが発生する問題
- ワールド移動時にスコアボードの描写が更新されなくなる問題
- GUIの画面内にアイテムが投入できてしまう問題
- スコアボード上の`次のランクまで`の数値の問題

## 修正できていないかもしれない問題
- サーバー移動後に暗視の効果が切れない問題?