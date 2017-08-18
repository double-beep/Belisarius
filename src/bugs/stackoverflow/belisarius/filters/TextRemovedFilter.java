package bugs.stackoverflow.belisarius.filters;

import bugs.stackoverflow.belisarius.models.Post;
import bugs.stackoverflow.belisarius.utils.CheckUtils;

public class TextRemovedFilter implements Filter {

	private Post post;
	private final double percentage = 0.8;
	private double score;

	public TextRemovedFilter(Post post) {
		this.post = post;
	}
	
	@Override
	public boolean isHit() {
		String original = "";
		String target = "";
		
		if (this.post.getBody() != null && this.post.getLastBody() != null) {
			original = this.post.getLastBody();
			target = this.post.getBody();
		}

		this.score = CheckUtils.getJaroWiklerScore(original, target, percentage);
		
		return this.score < 0.6;
	}

	@Override
	public double getScore() {
		return 1.0;
	}

	@Override
	public String getDescription() {
		return "**" + percentage*100 + "% or more text removed with a JW score of " + Math.round(this.score*100.0)/100.0  + "**";
	}

	@Override
	public Severity getSeverity() {
		return Severity.MEDIUM;
	}

}
