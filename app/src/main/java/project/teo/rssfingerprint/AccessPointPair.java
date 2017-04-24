package project.teo.rssfingerprint;

/**
 * Created by teo on 4/24/17.
 */
public class AccessPointPair {

    private String BSSID;
    private String SSID;
    public AccessPointPair(String BSSID, String SSID) {
        this.BSSID = BSSID;
        this.SSID = SSID;
    }
    @Override
    public boolean equals(Object obj) {
        AccessPointPair ap = (AccessPointPair) obj;
        if (ap.BSSID.equals(BSSID) && ap.SSID.equals(SSID)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        //Josh Blosh effective Java
        return ( 37 * 4 + BSSID.hashCode() ) +
                ( 37 * 4 + SSID.hashCode() );
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }
}
