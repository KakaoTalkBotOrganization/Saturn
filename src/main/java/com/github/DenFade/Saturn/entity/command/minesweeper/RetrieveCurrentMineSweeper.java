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
import java.util.concurrent.TimeUnit;

@CommandProperties(level = PermissionLevel.VERIFIED)
@CommandDoc(
        alias = "ms.r",
        preview = "지뢰찾기 메시지 끌어오기"
)
@SuppressWarnings("ConstantConditions")
public class RetrieveCurrentMineSweeper extends IGuildCommand {

    @Override
    public void run(IGuildMessageReceivedEvent event) {
        super.run(event);

        event.getMessage().delete().queueAfter(1000, TimeUnit.MILLISECONDS);

        String mergedIds = event.getConfiguration().getId() + event.getTextChannel().getId();
        boolean b = Objects.requireNonNull(
                DataBase.minesweeperChannelDB.extract(db -> {
                    return !db.isNull(event.getConfiguration().getId()) && db.getJSONArray(event.getConfiguration().getId()).toList().contains(event.getTextChannel().getId());
                }));
        if(b){
            if(Bot.minesweeperCenter().has(mergedIds)){
                final MineSweeper ms = Bot.minesweeperCenter().find(mergedIds);
                final String messageId = Bot.minesweeperCenter().find(mergedIds).getMessageId();
                MineSweeper.Display display = MineSweeper.Display.ONGOING;
                Translator.doOnTranslate(event.getConfiguration().getLang(), "ms_ongoing_script", event, (s, ievent) -> {
                    ievent.getTextChannel().deleteMessageById(messageId).queue();
                    ievent.getTextChannel().sendMessage(
                            String.format(s, ms.getX(), ms.getY(), ms.getBomb(), EmojiFactory.ANGRY_FACE_WITH_HORNS.getEmoji(), ms.getRate(), display, ms.display(display, 0, 0), ms.getLeftBomb(), ms.getParticipants().size(), Utils.timeIndicator(System.currentTimeMillis() - ms.getStartAt()))
                    ).queue(m -> {
                        String newMessageId = m.getId();
                        ms.setMessageId(newMessageId);

                        Bot.minesweeperCenter().register(mergedIds, ms);
                    });
                });
            } else {
                Translator.doOnTranslate(event.getConfiguration().getLang(), "ms_open_no_game", event, (s, ievent) -> {
                    ievent.getTextChannel().sendMessage(String.format(s, EmojiFactory.CONFUSED_FACE.getEmoji())).queue(m -> m.addReaction(EmojiFactory.WHITE_CHECK_MARK.getEmoji()).queue());
                });
            }
        } else {
            Translator.doOnTranslate(event.getConfiguration().getLang(), "ms_start_unregistered_channel", event, (s, ievent) -> {
                ievent.getTextChannel().sendMessage(String.format(s, ievent.getGuildChannel().getId(), EmojiFactory.SAD_BUT_RELIEVED_FACE.getEmoji())).queue(m -> m.addReaction(EmojiFactory.WHITE_CHECK_MARK.getEmoji()).queue());
            });
        }
    }
}
