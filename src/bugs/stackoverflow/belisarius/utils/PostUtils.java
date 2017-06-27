package bugs.stackoverflow.belisarius.utils;

import bugs.stackoverflow.belisarius.filters.*;
import bugs.stackoverflow.belisarius.models.*;
import bugs.stackoverflow.belisarius.services.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class PostUtils {
	
	private ApiService apiService;
	
	public PostUtils() {
		
		PropertyService ps = new PropertyService();
		apiService = new ApiService(ps.getSite());
	}
	
	public List<Integer> getPostIdsByActivity(long lastPostTime) {
		List<Integer> postIds = new ArrayList<Integer>();
		
		JsonObject postsJSON = null;
		try {
			long lastActivityDate = 0;
			long maxActivityDate = lastPostTime;
			int page = 1;
			do {
				postsJSON = apiService.getPostIdsByActivityDesc(page);
	            for (JsonElement post : postsJSON.get("items").getAsJsonArray()) {
            		lastActivityDate = post.getAsJsonObject().get("last_activity_date").getAsLong();
	            	
            		if (postBeenEdited(post) && editorAlsoOwner(post) && lastPostTime < lastActivityDate) {
	            		int postId = post.getAsJsonObject().get("post_id").getAsInt();
	            		if (!postIds.contains(postId)) {
	            			postIds.add(postId);
	            		}
	            	}
            		
	                if (maxActivityDate < lastActivityDate) {
	            		maxActivityDate = lastActivityDate;
	            	}
	            }
	            page++;
			} while (lastPostTime < lastActivityDate & page<10);
		
           	MonitorService.lastPostTime = maxActivityDate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return postIds;
	}
	
	private boolean postBeenEdited(JsonElement post) {
		if (post.getAsJsonObject().has("last_edit_date")) {
			long lastActivityDate = post.getAsJsonObject().get("last_activity_date").getAsLong();
			long lastEditDate = post.getAsJsonObject().get("last_edit_date").getAsLong();
			if(lastActivityDate == lastEditDate) {
				return true;
			}
		}
		
		return false; 
	}
	
	private boolean editorAlsoOwner(JsonElement post) {
		try{
			if (!post.getAsJsonObject().has("owner")) {
				return true;
			} else {
				JsonObject ownerJSON = post.getAsJsonObject().get("owner").getAsJsonObject();
				JsonObject editorJSON = post.getAsJsonObject().get("last_editor").getAsJsonObject();
				
				long ownerId = ownerJSON.get("user_id").getAsLong();
				long editorId = editorJSON.get("user_id").getAsLong();
				
				if (ownerId == editorId) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public List<Post> getLastestRevisions(String postIdInput) {
		List<Post> revisions = new ArrayList<Post>();
		String[] postIds = postIdInput.split(";");
		boolean hasMore = false;
		do {
			
			try {
				JsonObject postsJSON = apiService.getLastestRevisions(String.join(";", postIds));
				hasMore = postsJSON.get("has_more").getAsBoolean();
				for (String id : postIds) {
					int revisionNo = 0;
					Post revision = null;
					for (JsonElement post : postsJSON.get("items").getAsJsonArray()) {
					    if (post.getAsJsonObject().get("post_id").getAsInt() == Integer.parseInt(id) && post.getAsJsonObject().has("revision_number")) {
					    	if (revisionNo < post.getAsJsonObject().get("revision_number").getAsInt()) {
					    		revisionNo = post.getAsJsonObject().get("revision_number").getAsInt();
					    		revision = getPost(post.getAsJsonObject());
					    	}
					    }
					}
				    if (revision != null) {
				    	revisions.add(revision);
				    }
					postIds = ArrayUtils.removeElement(postIds, id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} while (hasMore || postIds.length>0);
		
		return revisions;
	}
	
	public static Post getPost(JsonObject post){

        Post np = new Post();

        np.setPostID(post.get("post_id").getAsInt());

        np.setRevisionNumber(post.get("revision_number").getAsInt());
        
        if (post.has("body")) {
	        np.setBody(post.get("body").getAsString());
        }
        
        if (post.has("last_body")) {
	        np.setLastBody(post.get("last_body").getAsString());
        }
        
        if (post.has("title")) {
	        np.setTitle(post.get("title").getAsString());
        }
        
        if (post.has("last_title")) {
            np.setLastBody(post.get("last_title").getAsString());
        }
        
        np.setIsRollback(post.get("is_rollback").getAsBoolean());
        np.setPostType(post.get("post_type").getAsString());
        
        JsonObject userJSON = post.get("user").getAsJsonObject();
        SOUser user = new SOUser();
       
        try{
        	user.setReputation(userJSON.get("reputation").getAsLong());
        	user.setUsername(JsonUtils.escapeHtmlEncoding(userJSON.get("display_name").getAsString()));
        	user.setUserType(userJSON.get("user_type").getAsString());
        	user.setUserId(userJSON.get("user_id").getAsInt());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        np.setUser(user);
        
        return np;
    }
	
    public static VandalisedPost getVandalisedPost(Post post) {
        List<Filter> filters = new ArrayList<Filter>(){{
            add(new BlacklistedFilter(post));
            add(new VeryLongWordFilter(post));
            add(new CodeRemovedFilter(post));
            add(new TextRemovedFilter(post));
            add(new FewUniqueCharactersFilter(post));
        }};
        
        Map<String, Double> reasons = new HashMap<String, Double>();
        for(Filter filter: filters){
            if(filter.isHit()){
            	reasons.put(filter.getDescription(), filter.getScore());
            }
        }

        return new VandalisedPost(post, reasons);
    }
	
}