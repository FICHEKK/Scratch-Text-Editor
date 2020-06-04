package texteditor.location;

public class LocationRange
{
    private Location start;
    private Location end;

    public LocationRange(Location start, Location end)
    {
        this.start = start;
        this.end = end;
    }

    public boolean isEmpty()
    {
        return start.equals(end);
    }

    public Location getStart()
    {
        if(start.row < end.row) return start;
        if(start.row > end.row) return end;

        return start.column <= end.column ? start : end;
    }

    public Location getEnd()
    {
        if(start.row < end.row) return end;
        if(start.row > end.row) return start;

        return start.column <= end.column ? end : start;
    }

    public void setStart(Location start)
    {
        this.start = start;
    }

    public void setEnd(Location end)
    {
        this.end = end;
    }
}
