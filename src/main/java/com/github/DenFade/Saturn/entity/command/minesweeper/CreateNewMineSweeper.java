package com.github.DenFade.Saturn.entity.command.minesweeper;

import com.github.DenFade.Saturn.Bot;
import com.github.DenFade.Saturn.database.DataBase;
import com.github.DenFade.Saturn.entity.annotation.command.CommandDoc;
import com.github.DenFade.Saturn.entity.annotation.command.CommandProperties;
import com.github.DenFade.Saturn.entity.command.IGuildCommand;
import com.github.DenFade.Saturn.entity.game.MineSweeper;
import com.github.DenFade.Saturn.event.IGuildMessageReceivedEvent;
import com.github.DenFade.Saturn.util.EmojiFactory;
import com.github.DenFade.Saturn.util.PermissionLevel;
import com.github.DenFade.Saturn.util.Translator;
import com.github.DenFade.Saturn.util.Utils;

import java.util.Objects;

@CommandProperties(level = PermissionLevel.VERIFIED)
@CommandDoc(
        alias = "ms.new",
        preview = "새 지뢰찾기 생성"
)
public class CreateNewMineSweeper extends IGuildCommand {

    @Override
    public void run(IGuildMessageReceivedEvent event) {
        super.run(event);

        String mergedIds = event.getConfiguration().getId() + event.getTextChannel().getId();
        String[] splitted = event.getSplitted();
        boolean b = Objects.requireNonNull(
                DataBase.minesweeperChannelDB.extract(db -> {
                    return !db.isNull(event.getConfiguration().getId()) && db.getJSONArray(event.getConfiguration().getId()).toList().contains(event.getTextChannel().getId());
                }));
        if(b){
            if (!Bot.minesweeperCenter().has(mergedIds)) {
                final int x, y, rate, bomb;
                int x1, y1, rate1;
                switch (splitted.length) {
                    case 2:
                        x1 = Utils.safeInt(splitted[1], Utils.random(8, 13));
                        if (x1 < 8) x1 = 8;
                        if (x1 > 13) x1 = 13;
                        y1 = Utils.random(8, 13);
                        rate1 = Utils.random(1, 20);
                        break;
                    case 3:
                        x1 = Utils.safeInt(splitted[1], Utils.random(8, 13));
                        y1 = Utils.safeInt(splitted[2], Utils.random(8, 13));
                        if (x1 < 8) x1 = 8;
                        if (x1 > 13) x1 = 13;
                        if (y1 < 8) y1 = 8;
                        if (y1 > 13) y1 = 13;
                        rate1 = Utils.random(1, 20);
                        break;
                    case 4:
                        x1 = Utils.safeInt(splitted[1], Utils.random(8, 13));
                        y1 = Utils.safeInt(splitted[2], Utils.random(8, 13));
                        rate1 = Utils.safeInt(splitted[3], Utils.random(11, 30));
                        if (x1 < 8) x1 = 8;
                        if (x1 > 13) x1 = 13;
                        if (y1 < 8) y1 = 8;
                        if (y1 > 13) y1 = 13;
                        if (rate1 < 1) rate1 = 1;
                        if (rate1 > 20) rate1 = 20;
                        break;
                    default:
                        x1 = Utils.random(8, 13);
                        y1 = Utils.random(8, 13);
                        rate1 = Utils.random(1, 20);
                }
                x = x1;
                y = y1;
                rate = rate1 + 10;
                bomb = ((x * y * rate) / 100) + 1;
                MineSweeper ms = new MineSweeper(x, y, bomb, event.getUser().getId());
                Bot.minesweeperCenter().register(mergedIds, ms);

                Translator.doOnTranslate(event.getConfiguration().getLang(), "ms_start_create_new", event, (s, ievent) -> {
                    ievent.getTextChannel().sendMessage(String.format(s, x, y, bomb, EmojiFactory.ANGRY_FACE_WITH_HORNS.getEmoji(), rate - 10)).queue();
                });
            } else {
                Translator.doOnTranslate(event.getConfiguration().getLang(), "ms_start_already_started", event, (s, ievent) -> {
                    ievent.getTextChannel().sendMessage(String.format(s, EmojiFactory.CONFUSED_FACE.getEmoji())).queue();
                });
            }
        } else {
            Translator.doOnTranslate(event.getConfiguration().getLang(), "ms_start_unregistered_channel", event, (s, ievent) -> {
                ievent.getTextChannel().sendMessage(String.format(s, ievent.getGuildChannel().getId(), EmojiFactory.SAD_BUT_RELIEVED_FACE.getEmoji())).queue(m -> m.addReaction(EmojiFactory.WHITE_CHECK_MARK.getEmoji()).queue());
            });
        }
    }

}