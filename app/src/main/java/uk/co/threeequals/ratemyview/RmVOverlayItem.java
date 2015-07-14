package uk.co.threeequals.ratemyview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

public class RmVOverlayItem extends OverlayItem implements Parcelable{
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
	public String dbId;
	private String nonce;
	private String tsVague;
	private String photoData;
		
	public RmVOverlayItem(String aTitle, String aDescription,
	GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
		fromDB = false;
	}
	
	public RmVOverlayItem(ViewORM savedObj) {
		super("", "", new GeoPoint(Double.parseDouble(savedObj.lat), Double.parseDouble(savedObj.lng)));
		//savedObj.
		//Fill out the object using values from the Passed in Object
		age = savedObj.age;
		comments = savedObj.comments;
		heading = savedObj.heading;
		ts = savedObj.ts;
		rating = savedObj.rating;
		words = new String[]{savedObj.word1, savedObj.word2, savedObj.word3};
		know = savedObj.know;
		time = savedObj.time;
		nonce = savedObj.nonce;
		fromDB = true;
		
		photoLocation = savedObj.photoLocation;
		File imgFile = new File(photoLocation);
		//System.out.println("Photo location: " + photoLocation);
		if(imgFile.exists()){
			//System.out.println("Image exists");
			try {
				FileInputStream fin = new FileInputStream(imgFile.getAbsolutePath());
				byte fileContent[] = new byte[(int)imgFile.length()];
				// Reads up to certain bytes of data from this input stream into an array of bytes.
				fin.read(fileContent);
				//System.out.println("Read file");

				fin.close();
				photoData = Base64.encodeToString(fileContent, Base64.DEFAULT);
			} catch (FileNotFoundException e) {
				System.out.println("Cannot find image");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IO exception");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		 
			

			//img.setImageBitmap(myBitmap);
		} else {
			System.out.println("Cannot find image");
			//TODO - Add throw?
		}
	}
	
	public void setId(String aId){
		id = aId;
	}
	public String getId(){
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
	
	public void setTsVague(String aTsVague){
		tsVague = aTsVague;
	}
	public String getTsVague(){
		return tsVague;
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
	
	public void setPhotoData(String aPhotoData){
		photoData = aPhotoData;
	}
	public String getPhotoData(){
		return photoData;
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
	
	public Float getLat(){
		return (float) (this.getPoint().getLatitudeE6() / 1E6);
	}
	
	public Float getLng(){
		return (float) (this.getPoint().getLongitudeE6() / 1E6);
	}
	

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
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
		super("", "", new GeoPoint(0, 0));
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