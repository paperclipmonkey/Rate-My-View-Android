package uk.co.threeequals.ratemyview;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.orm.SugarRecord;

public class RmVOverlayItem extends SugarRecord implements ClusterItem, Parcelable{
    private Long id;
    private String stringId;
	private String photo;
	private String age;
	private String comments;
	private long heading;
	private String ts;
	private Integer rating;
    private String words1;
    private String words2;
    private String words3;
	private String know;
	private String photoLocation;
	private String time;
	private String nonce;
	private String tsVague;
    private Double lng;
    private Double lat;

    public RmVOverlayItem(){
        id = 0L;
    }

    public void setId(Long aId){
        id = aId;
    }
    public Long getId(){
        return id;
    }


	public void setStringId(String aId){
        stringId = aId;
	}

	public String getStringId(){
		return stringId;
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
	
	public void setWords1(String aWords){
		words1 = aWords;
	}
	public String getWords1(){
		return words1;
	}

    public void setWords2(String aWords){
        words2 = aWords;
    }
    public String getWords2(){
        return words2;
    }

    public void setWords3(String aWords){
        words3 = aWords;
    }
    public String getWords3(){
        return words3;
    }

    public void setWordsArray(String[] aWords){
        if(aWords.length > 0) {
            words1 = aWords[0];
        }
        if(aWords.length > 1) {
            words2 = aWords[1];
        }
        if(aWords.length > 2) {
            words3 = aWords[2];
        }
    }

    public String[] getWordsArray(){
        return new String[]{words1, words2, words3};
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
        return new LatLng(lat, lng);
    }

    public void setPosition(LatLng aPosition){
        lat = aPosition.latitude;
        lng = aPosition.longitude;
    }

	public Double getLat(){
        return lat;
	}

	public Double getLng(){
        return lng;
	}

    public void setLat(Double aLat) { lat = aLat; }
    public void setLng(Double aLng){
        lng = aLng;
    }

    @Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
        out.writeString(stringId);
        out.writeLong(id);
		out.writeString(photo);
		out.writeString(age);
		out.writeString(comments);
		out.writeInt((int) heading);
		out.writeString(ts);
		out.writeInt(rating);
		out.writeString(tsVague);
        out.writeString(words1);
        out.writeString(words2);
        out.writeString(words3);
		out.writeString(know);
		out.writeString(photoLocation);
		out.writeString(time);
        out.writeString(nonce);
        out.writeDouble(lat);
        out.writeDouble(lng);
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
        stringId = in.readString();
        id = in.readLong();
		photo = in.readString();
		age = in.readString();
		comments = in.readString();
		heading = in.readInt();
		ts = in.readString();
		rating = in.readInt();
		tsVague = in.readString();
        words1 = in.readString();
        words2 = in.readString();
        words3 = in.readString();
        know = in.readString();
		photoLocation = in.readString();
		time = in.readString();
        nonce = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
	}
}