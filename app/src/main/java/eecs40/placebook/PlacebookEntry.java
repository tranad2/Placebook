package eecs40.placebook;

import android.os.Parcel;
import android.os.Parcelable;

public class PlacebookEntry implements Parcelable {
    public final long id;
    private String name;
    private String description;
    private String photoPath;

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

    public void setPhotoPath(String path){
        photoPath = path;
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