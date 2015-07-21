package uk.co.threeequals.ratemyview;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.orm.SugarRecord;

public class RmVOverlayItem extends SugarRecord implements ClusterItem, Parcelable{
	private String id;
	private String photo;
	private String age;
	private String comments;
	private long heading;
	private String ts;
	private Integer rating;
	private String[] words;
	private String know;
	private String photoLocation;
	private String time;
	public Boolean fromDB;
	private String nonce;
	private String tsVague;
    private LatLng position;

	public RmVOverlayItem(String aTitle, String aDescription,
						  LatLng aGeoPoint) {
		//super(aTitle, aDescription, aGeoPoint);
		fromDB = false;
	}

    public RmVOverlayItem(){

    }
	
	public void setStringId(String aId){
		id = aId;
	}
	public String getStringId(){
		return id;
	}
	
	public void setNonce(String aNonce){
		nonce = aNonce;
	}

	public String getNonce(){
		return nonce;
	}

	public void setPhoto(String aPhoto){
		photo = aPhoto;
	}
	public String getPhoto(){
		return photo;
	}
	
	public void setComments(String aComments){
		comments = aComments;
	}
	public String getComments(){
		return comments;
	}
	
	public void setAge(String aAge){
		age = aAge;
	}
	public String getAge(){
		return age;
	}

	public void setHeading(long aHeading){
		heading = aHeading;
	}
	public long getHeading(){
		return heading;
	}
	
	public void setTs(String aTs){
		ts = aTs;
	}
	public String getTs(){
		return ts;
	}
	
	public void setRating(Integer aRating){
		rating = aRating;
	}
	public Integer getRating(){
		return rating;
	}
	
	public void setWords(String[] aWords){
		words = aWords;
	}
	public String[] getWords(){
		return words;
	}
	
	public void setKnow(String aKnow){
		know = aKnow;
	}
	public String getKnow(){
		return know;
	}

	public void setPhotoLocation(String aPhotoLocation){
		photoLocation = aPhotoLocation;
	}
	public String getPhotoLocation(){
		return photoLocation;
	}
	
	public void setTime(String aTime){
		time = aTime;
	}
	
	public String getTime(){
		return time;
	}

    @Override
    public LatLng getPosition(){
        return position;
    }

    public void setPosition(LatLng aPosition){
        position = aPosition;
    }


//	public Float getLat(){
//		return (float) (this.getPoint().getLatitudeE6() / 1E6);
//	}
//
//	public Float getLng(){
//		return (float) (this.getPoint().getLongitudeE6() / 1E6);
//	}
//

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(photo);
		out.writeString(age);
		out.writeString(comments);
		out.writeInt((int)heading);
		out.writeString(ts);
		out.writeInt(rating);
		out.writeString(tsVague);
		out.writeStringArray(words);
		out.writeString(know);
		out.writeString(photoLocation);
		out.writeString(time);
		out.writeString(nonce);
	}
	
	public static final Parcelable.Creator<RmVOverlayItem> CREATOR = new Parcelable.Creator<RmVOverlayItem>() {
		public RmVOverlayItem createFromParcel(Parcel in) {
		    return new RmVOverlayItem(in);
		}
		
		public RmVOverlayItem[] newArray(int size) {
		    return new RmVOverlayItem[size];
		}
	};

	private RmVOverlayItem(Parcel in) {
		id = in.readString();
		photo = in.readString();
		age = in.readString();
		comments = in.readString();
		heading = in.readInt();
		ts = in.readString();
		rating = in.readInt();
		tsVague = in.readString();
		words = in.createStringArray();
		know = in.readString();
		photoLocation = in.readString();
		time = in.readString();
		nonce = in.readString();
	}
}