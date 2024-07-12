package com.ecoss.hud_test_resolution.utilities;
public class LambertConverter {

    private final String TAG = "EDC_TEST";

    private static final int NX = 149; /* X축 격자점 수 */
    private static final int NY = 253; /* Y축 격자점 수 */

    double Re; /* 사용할 지구반경 [ km ] */
    double grid; /* 격자간격 [ km ] */
    double slat1; /* 표준위도 [degree] */
    double slat2; /* 표준위도 [degree] */
    double olon; /* 기준점의 경도 [degree] */
    double olat; /* 기준점의 위도 [degree] */
    double xo; /* 기준점의 X좌표 [격자거리] */
    double yo; /* 기준점의 Y좌표 [격자거리] */
    // int first; /* 시작여부 (0 = 시작) */

    double PI = Math.asin(1.0f) * 2.0f;
    double DEGRAD = PI / 180.0f;

    double re, sn, sf, ro;
    double ra, theta;

    LambertConverter() {
        // 단기예보 지도 정보
        this.Re = 6371.00877f; // 지도반경
        this.grid = 5.0f; // 격자간격 (km)
        this.slat1 = 30.0f; // 표준위도 1
        this.slat2 = 60.0f; // 표준위도 2
        this.olon = 126.0f; // 기준점 경도
        this.olat = 38.0f; // 기준점 위도
        this.xo = 210 / grid; // 기준점 X좌표
        this.yo = 675 / grid; // 기준점 Y좌표


        re = Re / grid;
        slat1 = slat1 * DEGRAD;
        slat2 = slat2 * DEGRAD;
        olon = olon * DEGRAD;
        olat = olat * DEGRAD;

        sn = Math.tan(PI * 0.25 + slat2 * 0.5) / Math.tan(PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        sf = Math.tan(PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        ro = Math.tan(PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
    }

    /* 좌표변환 */
    int[] convertToXY(double lat, double lon) {
        int[] xy = new int[2];

        double[] txy = lamcproj(lat, lon);
        xy[0] = (int) (txy[0] + 1.5);
        xy[1] = (int) (txy[1] + 1.5);

        return xy;
    }

    /* 람베르트 좌표계 프로젝션 */
    double[] lamcproj(double lat, double lon) {

        double[] xy = new double[2];

        ra = Math.tan(PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        theta = lon * DEGRAD - olon;
        if (theta > PI) theta -= 2.0 * PI;
        if (theta < -PI) theta += 2.0 * PI;
        theta *= sn;

        xy[0] = (ra * Math.sin(theta)) + xo;
        xy[1] = (ro - ra * Math.cos(theta)) + yo;

        return xy;
    }
}
