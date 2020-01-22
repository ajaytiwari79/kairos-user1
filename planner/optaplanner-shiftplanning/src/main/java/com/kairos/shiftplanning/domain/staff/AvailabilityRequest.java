package com.kairos.shiftplanning.domain.staff;

import com.kairos.shiftplanning.utils.JodaTimeConverter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;

@Getter
@Setter
@NoArgsConstructor
@XStreamAlias("Availability")
public class AvailabilityRequest {
    private static Logger log= LoggerFactory.getLogger(AvailabilityRequest.class);
	private Long id;
    @XStreamConverter(JodaTimeConverter.class)
	private DateTime startTime;
	@XStreamConverter(JodaTimeConverter.class)
	private DateTime endTime;
	private boolean autogenerated=false;
	private Employee employee;


	public AvailabilityRequest(Long id, DateTime startTime, DateTime endTime,Employee employee) {

        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.employee=employee;
    }

    public AvailabilityRequest(Employee employee, Interval interval) {
        //log.info("Creating AvailabilityRequest using insertLogical");
        this.id = new Random().nextLong();
        autogenerated=true;
        this.employee=employee;
        if(interval!=null){
            this.startTime = interval.getStart();
            this.endTime = interval.getEnd();
        }

    }

	public long getMinutes(){
		//08/06/2017 11:00:00
		//LocalDateTime startDateTime = LocalDateTime.parse(startTimeString, formatter);
		//LocalDateTime endDateTime = LocalDateTime.parse(endTimeString, formatter);
		return getInterval().toDuration().getStandardMinutes();
	}
	public Interval getInterval(){
		return new Interval(getStartTime(),getEndTime());
	}
	@Override
	public String toString() {
		return "AvailabilityRequest [startTimeString=" + ", endTimeString=:" + getIntervalAsString()+",Emp:"+(employee!=null?employee.getId().toString():"")+ "]";
	}
	public String getIntervalAsString(){
		return ""+getStartTime().toString("HH:mm")+"-"+getEndTime().toString("HH:mm");
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AvailabilityRequest that = (AvailabilityRequest) o;

        if (!id.equals(that.id)) return false;
        if (!Objects.equals(startTime,that.startTime)) return false;
        if (!Objects.equals(endTime,that.endTime)) return false;
        if (!Objects.equals(employee,that.employee)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        if(startTime!=null){
            result = 31 * result + startTime.hashCode();
        }
        if(endTime!=null){
            result = 31 * result + endTime.hashCode();
        }
        if(employee!=null){
            result = 31 * result + employee.hashCode();
        }

        return result;
    }

	public boolean containsInterval(Interval interval){
		return interval!=null && this.getInterval().contains(interval);
	}
	/*public String getStartTimeString(){
		return startTime.toString("MM/dd/yyyy HH:mm:ss");
	}
	public String getEndTimeString(){
		return endTime.toString("MM/dd/yyyy HH:mm:ss");
	}*/

	public boolean overlaps(Interval interval) {
		return this.getInterval()!=null && this.getInterval().overlaps(interval);
	}
}
