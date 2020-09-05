package bugs.stackoverflow.belisarius.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bugs.stackoverflow.belisarius.models.Post;
import bugs.stackoverflow.belisarius.utils.CheckUtils;
import bugs.stackoverflow.belisarius.utils.DatabaseUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.chatexchange.chat.Room;

public class BlacklistedFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistedFilter.class);

    private Room room;
    private Post post;
    private int reasonId;
    private Map<Integer, String> blacklistedWordsTitle = new HashMap<>();
    private Map<Integer, String> blacklistedWordsBody = new HashMap<>();
    private Map<Integer, String> blacklistedWordsEditSummary = new HashMap<>();

    public BlacklistedFilter(Room room, Post post, int reasonId) {
        this.room = room;
        this.post = post;
        this.reasonId = reasonId;
    }

    @Override
    public boolean isHit() {

        if (post.getTitle() != null && "question".equals(post.getPostType())) {
            String titleDifference = StringUtils.difference(post.getLastTitle(), post.getTitle());
            blacklistedWordsTitle = CheckUtils.checkForBlackListedWords(titleDifference, "question_title");
        }

        if (post.getBody() != null) {
            String bodyDifference = StringUtils.difference(post.getLastBody(), post.getBody());
            blacklistedWordsBody = CheckUtils.checkForBlackListedWords(bodyDifference, post.getPostType());
        }

        if (post.getComment() != null) {
            blacklistedWordsEditSummary = CheckUtils.checkForBlackListedWords(post.getComment(), post.getPostType());
        }

        return getScore() > 0;
    }

    @Override
    public double getScore() {
        return this.blacklistedWordsTitle.size() + this.blacklistedWordsBody.size() + this.blacklistedWordsEditSummary.size();
    }

    @Override
    public String getFormattedReasonMessage() {
        String message = "";

        try {
            if (this.blacklistedWordsTitle.size() > 0) {
                message += "**Title contains blacklisted " + (this.blacklistedWordsTitle.size() > 1 ? "words" : "word") + ":** ";
                message += getBlacklistedWordsTitle() + " ";
            }

            if (this.blacklistedWordsBody.size() > 0) {
                message += "**Body contains blacklisted " + (this.blacklistedWordsBody.size() > 1 ? "words" : "word") + ":** ";
                message += getBlacklistedWordsBody() + " ";
            }

            if (this.blacklistedWordsEditSummary.size() > 0) {
                message += "**Edit summary contains blacklisted " + (this.blacklistedWordsEditSummary.size() > 1 ? "words" : "word") + ":** ";
                message += getBlacklistedWordsComment() + " ";
            }
        } catch (Exception exception) {
            LOGGER.info("Failed to get formatted reason message.", exception);
        }

        return message.trim();
    }

    @Override
    public String getReasonName() {
        String name = "Contains blacklisted words: ";
        if (this.blacklistedWordsTitle.size() > 0) {
            name += getBlacklistedWordsTitle();
        }
        if (this.blacklistedWordsBody.size() > 0) {
            name += getBlacklistedWordsBody();
        }
        if (this.blacklistedWordsEditSummary.size() > 0) {
            name += getBlacklistedWordsComment();
        }
        return name;
    }

    private String getBlacklistedWordsTitle() {
        StringBuilder words = new StringBuilder();

        for (String word : blacklistedWordsTitle.values()) {
            words.append(word);
        }

        return words.toString();
    }

    private String getBlacklistedWordsBody() {
        StringBuilder words = new StringBuilder();

        for (String word : blacklistedWordsBody.values()) {
            words.append(word);
        }

        return words.toString();
    }

    private String getBlacklistedWordsComment() {
        StringBuilder words = new StringBuilder();

        for (String word : blacklistedWordsEditSummary.values()) {
            words.append(word);
        }

        return words.toString();
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
    }

    private List<Integer> getCaughtBlacklistedWordIds() {
        List<Integer> blacklistedWordIds = new ArrayList<>();

        blacklistedWordIds.addAll(blacklistedWordsTitle.keySet());
        blacklistedWordIds.addAll(blacklistedWordsBody.keySet());
        blacklistedWordIds.addAll(blacklistedWordsEditSummary.keySet());

        return blacklistedWordIds;
    }

    @Override
    public void storeHit() {
        long postId = this.post.getPostId();
        int revisionNumber = this.post.getRevisionNumber();
        int roomId = this.room.getRoomId();
        if (!DatabaseUtils.checkReasonCaughtExists(postId, revisionNumber, roomId, this.reasonId)) {
            DatabaseUtils.storeReasonCaught(postId, revisionNumber, roomId, this.reasonId, this.getScore());
        }

        this.getCaughtBlacklistedWordIds().forEach(id -> {
            if (!DatabaseUtils.checkBlacklistedWordCaughtExists(postId, revisionNumber, roomId, id)) {
                DatabaseUtils.storeCaughtBlacklistedWord(postId, revisionNumber, roomId, id);
            }
        });
    }
}
