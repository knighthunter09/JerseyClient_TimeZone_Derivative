package nz;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

public class ReadCSV {

	private static final Calendar CALENDER_FROM = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	public static void main(String[] args) {
		ReadCSV obj = new ReadCSV();
		obj.run();

	}
	
	private class Data implements Serializable {
		   String dstOffset,
		   rawOffset,
		   status,
		   timeZoneId,
		   timeZoneName;

		public Data() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Data(String dstOffset, String rawOffset, String status,
				String timeZoneId, String timeZoneName) {
			super();
			this.dstOffset = dstOffset;
			this.rawOffset = rawOffset;
			this.status = status;
			this.timeZoneId = timeZoneId;
			this.timeZoneName = timeZoneName;
		}

		public String getDstOffset() {
			return dstOffset;
		}

		public void setDstOffset(String dstOffset) {
			this.dstOffset = dstOffset;
		}

		public String getRawOffset() {
			return rawOffset;
		}

		public void setRawOffset(String rawOffset) {
			this.rawOffset = rawOffset;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getTimeZoneId() {
			return timeZoneId;
		}

		public void setTimeZoneId(String timeZoneId) {
			this.timeZoneId = timeZoneId;
		}

		public String getTimeZoneName() {
			return timeZoneName;
		}

		public void setTimeZoneName(String timeZoneName) {
			this.timeZoneName = timeZoneName;
		}
	}

	public void run() {

		String csvFile = "\\input.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] input = line.split(cvsSplitBy);

				if (input.length == 3) {
					System.out.println(input[0] + "," + input[1] + ","
							+ input[2] + "," + timeZoneSetting(input[0],input[1],input[2]));
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String timeZoneSetting(String input,String latitude,String longitutde) throws URISyntaxException {
		String[] arrIn = input.split("\\s");
		StringBuffer formattedTime = new StringBuffer();
		
		if (arrIn.length == 2) {
			String date[] = arrIn[0].split("-");
			String time[] = arrIn[1].split(":");
			if (date.length == 3 && time.length == 3) {				
				
				
				//Rest
				Client client = ClientBuilder.newClient();
				URI uri = new URI("https://maps.googleapis.com/maps/api/timezone/json?location="+ latitude +","
						+ longitutde +"&timestamp="+ (int)CALENDER_FROM.getTimeInMillis() +"&key=AIzaSyC3t3t6Henbb96JhzrjJyWwwQoVZ9xDw2w");
				WebTarget target = client.target(uri);
 
				Response rs = target.request(MediaType.APPLICATION_JSON).get(Response.class);
				if (rs.getStatus() == 200) {
					Gson gson = new Gson();
					String entity = rs.readEntity(String.class);
					if (entity != null && !entity.isEmpty()) {
					}
					Data data = gson.fromJson(entity, Data.class);			
					LocalDateTime leaving = LocalDateTime.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]), 
							Integer.valueOf(time[0]), Integer.valueOf(time[1]), Integer.valueOf(time[2]));
					ZoneId leavingZone = ZoneId.of("UTC"); 
					ZonedDateTime departure = ZonedDateTime.of(leaving, leavingZone);
					
				    ZoneId arrivingZone = ZoneId.of(data.timeZoneId); 
				    ZonedDateTime arrival = departure.withZoneSameInstant(arrivingZone);

				    String out2 = arrival.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
				        
					formattedTime.append(data.getTimeZoneId());
					formattedTime.append("," + out2);
				}
			}
		}
		return formattedTime.toString();
	}
}