package com.threeequals.ratemyview;

import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name = "Views")
public class ViewORM extends Model {
	@Column(name = "_id")
	String _id;
	
	@Column(name = "photo")
	String photo;
	
	@Column(name = "age") 
	String age;
	
	@Column(name = "comments")
	String comments;
	
	@Column(name = "heading")
	long heading;
	
	@Column(name = "ts")
	String ts;
	
	@Column(name = "rating")
	Integer rating;
	
	@Column(name = "word1")
	String word1;
	
	@Column(name = "word2")
	String word2;
	
	@Column(name = "word3")
	String word3;
	
	@Column(name = "know")
	String know;
	
	@Column(name = "photoLocation")
	String photoLocation;
	
	@Column(name = "time")
	String time;
	
	@Column(name = "lat")
	String lat;
	
	@Column(name = "lng")
	String lng;

	@Column(name = "nonce")
	String nonce;
	
	public ViewORM(){
        super();
	}
	
	public ViewORM(String id, String photo){
	        super();
	        this._id = id;
	        this.photo = photo;
	}
	
	public ViewORM(RmVOverlayItem rmvOverlayItem) {
        super();
        _id = rmvOverlayItem.getId();
    	photo = rmvOverlayItem.getPhoto();
    	age = rmvOverlayItem.getAge();
    	comments = rmvOverlayItem.getComments();
    	heading = rmvOverlayItem.getHeading();
    	ts = rmvOverlayItem.getTs();
    	rating = rmvOverlayItem.getRating();
    	word1 = rmvOverlayItem.getWords()[0];
    	word2 = rmvOverlayItem.getWords()[1];
    	word3 = rmvOverlayItem.getWords()[2];
    	know = rmvOverlayItem.getKnow();
    	photoLocation = rmvOverlayItem.getPhotoLocation();
    	time = rmvOverlayItem.getTime();
    	Double ilat = rmvOverlayItem.getPoint().getLatitudeE6() / 1E6;
    	lat = ilat.toString();
    	Double ilng = rmvOverlayItem.getPoint().getLongitudeE6() / 1E6;
    	lng = ilng.toString();
    	nonce = rmvOverlayItem.getNonce();
		// TODO Auto-generated constructor stub
	}
	public static List<ViewORM> getUnsaved() {
		return new Select()
			.from(ViewORM.class)
			.where("lat != ''")//Blank _ID means not saved remotely
			.orderBy("id") 
			.execute();
	}
	
	public static void Del(String id){
		System.out.println("Deleting View: " + id);
		new Delete()
		.from(ViewORM.class)
		.where("photoLocation = ?", id)//ID for Db row
		.execute();
	}

}