package fr.cls.atoll.motu.processor.wps.framework;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import fr.cls.atoll.motu.library.utils.DynamicEnumerable;
import fr.cls.atoll.motu.processor.wps.framework.MotuExecuteResponse.WPSStatusResponse;

/**
 * <br><br>Copyright : Copyright (c) 2009.
 * <br><br>Soci�t� : CLS (Collecte Localisation Satellites)
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-10-15 14:38:09 $
 */
public class MotuWPSStatusType implements DynamicEnumerable<WPSStatusResponse> {
    private WPSStatusResponse status;

    private String value;
    public MotuWPSStatusType(WPSStatusResponse statusResponse, String value) {

        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }

        this.status = statusResponse;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public WPSStatusResponse enumValue() {
        return status;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return value;
    }
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(value)
               .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        } else if (!(obj instanceof MotuWPSStatusType)) {
            return false;
        }

        MotuWPSStatusType other = (MotuWPSStatusType) obj;
        return new EqualsBuilder().append(status, other.status)
               .append(value, other.value).isEquals();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
