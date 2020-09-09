package bugs.stackoverflow.belisarius.filters;

import bugs.stackoverflow.belisarius.models.Post;
import bugs.stackoverflow.belisarius.utils.CheckUtils;
import bugs.stackoverflow.belisarius.utils.DatabaseUtils;

import org.sobotics.chatexchange.chat.Room;

public class TextRemovedFilter implements Filter {

    private Room room;
    private Post post;
    private int reasonId;
    private final double percentage = 0.8;
    private double score;

    public TextRemovedFilter(Room room, Post post, int reasonId) {
        this.room = room;
        this.post = post;
        this.reasonId = reasonId;
    }

    @Override
    public boolean isHit() {
        String original = "";
        String target = "";

        if (this.post.getBody() != null && this.post.getLastBody() != null) {
            original = this.post.getLastBody();
            target = this.post.getBody();
        }

        this.score = CheckUtils.getJaroWinklerScore(original, target, percentage);

        return this.score < 0.6;
    }

    @Override
    public double getScore() {
        return 1.0;
    }

    @Override
    public String getFormattedReasonMessage() {
        return "**" + percentage * 100 + "% or more text removed with a JW score of " + Math.round(this.score * 100.0) / 100.0 + "**";
    }

    @Override
    public String getReasonName() {
        return "Lots of text removed with a high JW score";
    }

    @Override
    public Severity getSeverity() {
        return Severity.MEDIUM;
    }

    @Override
    public void storeHit() {
        long postId = this.post.getPostId();
        int revisionNumber = this.post.getRevisionNumber();
        int roomId = this.room.getRoomId();
        if (!DatabaseUtils.checkReasonCaughtExists(postId, revisionNumber, roomId, this.reasonId)) {
            DatabaseUtils.storeReasonCaught(postId, revisionNumber, roomId, this.reasonId, this.getScore());
        }
    }
}
