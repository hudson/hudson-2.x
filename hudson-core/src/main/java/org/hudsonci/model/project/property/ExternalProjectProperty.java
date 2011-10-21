package org.hudsonci.model.project.property;

import org.hudsonci.api.model.IJob;

/**
 * Class property is intended to be used for ProjectProperties without correct equals and hashCode methods, such as
 * Builders, Publishers, etc.
 * <p/>
 * This property has additional {@link #modified} flag, that is used to define, whether current property was changed
 * from UI. If yes, cascading value will be updated. {@link #updateOriginalValue(Object, Object)} method for details.
 * <p/>
 * <p/>
 * Date: 10/20/11
 *
 * @author Nikita Levyankov
 */
public class ExternalProjectProperty<T> extends BaseProjectProperty<T> {

    private boolean modified;

    public ExternalProjectProperty(IJob job) {
        super(job);
    }

    /**
     * Method set modified state for current property.
     *
     * @param modified true if property was modified by user.
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * @return true if property was modified, false - otherwise.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * If property was not marked as {@link #isModified()} by calling {@link #setModified(boolean)} method with
     * true parameter value, than property will not be updated. This was implemented as the workaround for absent
     * equals methods for Publishers, BuildWrappers, etc.
     * <p/>
     * Such properties could be normally compared and use in cascading functionality.
     *
     * @param value new value to be set.
     * @param cascadingValue current cascading value.
     * @return true if value was updated, false - otherwise.
     */
    @Override
    protected boolean updateOriginalValue(T value, T cascadingValue) {
        return isModified() && super.updateOriginalValue(value, cascadingValue);
    }
}
