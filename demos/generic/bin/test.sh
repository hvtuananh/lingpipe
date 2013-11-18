sh gui_coref_en_news_muc6.sh 
sh gui_echo.sh 
sh gui_ne_en_bio_genetag.sh
sh gui_ne_en_bio_genia.sh
sh gui_ne_en_news_muc6.sh
sh gui_pos_en_bio_genia.sh
sh gui_pos_en_bio_medpost.sh
sh gui_pos_en_general_brown.sh
sh gui_sentence_en_bio.sh
sh gui_sentence_en_news.sh
sh gui_word_zh_as.sh

echo John Smith lives in Washington.  He likes it there. | cmd_coref_en_news_muc6.sh 
echo John Smith lives in Washington.  He likes it there. | sh cmd_echo.sh 
echo John Smith lives in Washington.  He likes it there. | sh cmd_ne_en_bio_genetag.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_ne_en_bio_genia.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_ne_en_news_muc6.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_pos_en_bio_genia.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_pos_en_bio_medpost.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_pos_en_general_brown.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_sentence_en_bio.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_sentence_en_news.sh
echo John Smith lives in Washington.  He likes it there. | sh cmd_word_zh_as.sh

