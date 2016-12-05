package com.bkromhout.balances.events;

/**
 * Event fired when some sort of click event happens for a {@link com.bkromhout.balances.data.models.Category} item.
 */
public class CategoryClickEvent {
    /**
     * Type of click.
     */
    public enum Type {
        NORMAL, LONG, ACTIONS
    }

    /**
     * What type of click this event is reporting.
     */
    private final Type type;
    /**
     * The unique ID value for the {@link com.bkromhout.balances.data.models.Category} represented by the clicked item.
     */
    private final long uniqueId;
    /**
     * The position of the clicked view.
     */
    private final int adapterPosition;
    /**
     * The layout position of the clicked view.
     */
    private final int layoutPosition;
    /**
     * If {@link #type} is {@link Type#ACTIONS}, this will be the action ID, otherwise it will be -1;
     */
    private final int actionId;

    /**
     * Create a new {@link CategoryClickEvent}.
     * @param type            Type of click.
     * @param uniqueId        The unique ID value for the {@link com.bkromhout.balances.data.models.Category}
     *                        represented by the clicked item.
     * @param adapterPosition The position of the clicked view.
     * @param layoutPosition  The layout position of the clicked view.
     * @param actionId        The action ID, if applicable.
     */
    public CategoryClickEvent(Type type, long uniqueId, int adapterPosition, int layoutPosition, int actionId) {
        this.type = type;
        this.uniqueId = uniqueId;
        this.adapterPosition = adapterPosition;
        this.layoutPosition = layoutPosition;
        this.actionId = actionId;
    }

    /**
     * Get the type of click this event represents.
     * @return Click type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the unique ID value for the {@link com.bkromhout.balances.data.models.Category} represented
     * by the clicked item.
     * @return {@code uniqueId} value.
     */
    public long getUniqueId() {
        return uniqueId;
    }

    /**
     * Get the adapter position of the clicked item.
     * @return Item adapter position.
     */
    public int getAdapterPosition() {
        return adapterPosition;
    }

    /**
     * Get the layout position of the clicked item.
     * @return Item layout position.
     */
    public int getLayoutPosition() {
        return layoutPosition;
    }

    /**
     * Get the action ID, if one of the action buttons was clicked.
     * @return The clicked action ID, or -1;
     */
    public int getActionId() {
        return actionId;
    }
}
