package bugs.stackoverflow.belisarius.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bugs.stackoverflow.belisarius.models.Post;
import bugs.stackoverflow.belisarius.utils.CheckUtils;
import bugs.stackoverflow.belisarius.utils.DatabaseUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeryLongWordFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(VeryLongWordFilter.class);

    private final int roomId;
    private final Post post;
    private final int reasonId = 6;
    private String listedWord;

    public VeryLongWordFilter(int chatroomId, Post post) {
        this.roomId = chatroomId;
        this.post = post;
    }

    @Override
    public boolean isHit() {
        this.listedWord = "";
        if (post.getBody() != null) {
            this.listedWord = CheckUtils.checkForLongWords(post.getBody());
            String oldListedWord = CheckUtils.checkForLongWords(post.getLastBody());
            return this.listedWord != null && oldListedWord == null;
        }
        return false;
    }

    @Override
    public double getScore() {
        return 1.0;
    }

    @Override
    public double getTotalScore() {
        return getScore();
    }

    @Override
    public String getFormattedReasonMessage() {
        return "**Contains very long word:** " + this.listedWord.substring(0, 40) + "...";
    }

    @Override
    public List<String> getReasonName() {
        return new ArrayList<>(Collections.singletonList("Contains very long word"));
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
    }

    @Override
    public void storeHit() {
        long postId = this.post.getPostId();
        int revisionNumber = this.post.getRevisionNumber();
        if (!DatabaseUtils.checkReasonCaughtExists(postId, revisionNumber, this.roomId, this.reasonId)) {
            DatabaseUtils.storeReasonCaught(postId, revisionNumber, this.roomId, this.reasonId, this.getScore());
            LOGGER.info("Successfully stored reason VeryLongWordFilter for post " + postId + " to database.");
        }
    }

}
