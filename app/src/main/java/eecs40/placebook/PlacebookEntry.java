package eecs40.placebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

public class PlacebookEntry implements Parcelable {
    public final long id;
    private String name;
    private String description;
    private String photoPath;

    public PlacebookEntry(long id){
        this.id = id;
        name = "";
        description = "";
        photoPath = "";
    }

    public PlacebookEntry(Parcel source) {
        this.id = source.readLong();
        this.name = source.readString();
        this.description = source.readString();
        this.photoPath = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name.toString());
        dest.writeString(this.description);
        dest.writeString(this.photoPath);
    }

    @Override
    public int describeContents () {
        return 0;
    }

    public void setName(String name){
        this.name = name;
    }

    public void appendDescription(String description){
        this.description+=description;
    }

    public void setPhotoPath(String path){
        photoPath = path;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public Bitmap getImage(){
        Bitmap myBitmap = null;
        if(photoPath != "")
             myBitmap = BitmapFactory.decodeFile(photoPath);
        return myBitmap;
    }

    public String toString(){
        String str = "ID: "+id+" Name: "+name+" Description: "+description+" photoPath: "+photoPath;
        return str;
    }

    public static final Parcelable.Creator<PlacebookEntry> CREATOR = new Parcelable.Creator<PlacebookEntry>() {
        @Override
        public PlacebookEntry createFromParcel(Parcel source) {
            return new PlacebookEntry(source);
        }
        @Override
        public PlacebookEntry [] newArray(int size) {
            return new PlacebookEntry[size];
        }
    };
}