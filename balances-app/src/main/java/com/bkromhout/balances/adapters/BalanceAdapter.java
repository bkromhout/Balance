package com.bkromhout.balances.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bkromhout.balances.R;
import com.bkromhout.balances.Utils;
import com.bkromhout.balances.data.models.Balance;
import com.bkromhout.rrvl.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Adapter class which handles binding {@link Balance}s to a RealmRecyclerView.
 */
public class BalanceAdapter extends RealmRecyclerViewAdapter<Balance, RecyclerView.ViewHolder> {
    /**
     * Whether or not the adapter should consider itself to be in selection mode.
     */
    private boolean inSelectionMode = false;

    /**
     * Create a new {@link BalanceAdapter}.
     * @param context      Context.
     * @param realmResults Results of a Realm query to display.
     */
    public BalanceAdapter(Context context, RealmResults<Balance> realmResults) {
        super(context, realmResults);
        setHasStableIds(true);
    }

    /**
     * Set whether or not the adapter should consider itself to be in selection mode. This is necessary to determine
     * what to do when an item is tapped.
     * @param enabled
     */
    public void setSelectionMode(boolean enabled) {
        this.inSelectionMode = enabled;
    }

    @Override
    public long getItemId(int position) {
        final Balance balance = realmResults.get(position);
        return balance.isValid() ? balance.uniqueId : -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BalanceVH(inflater.inflate(R.layout.balance_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BalanceVH vh = (BalanceVH) holder;
        final Balance balance = realmResults.get(position);
        if (!balance.isValid()) return;

        // Visually distinguish selected cards during multi-select mode.
        vh.content.setActivated(isSelected(position));

        // Set click handler.
        vh.content.setOnClickListener(view -> {
            if (inSelectionMode) {
                toggleSelected(holder.getAdapterPosition());
                return;
            }

            // TODO Send event to open the balance transactions list activity.
        });

        // Set long click handler.
        vh.content.setOnLongClickListener(view -> {
            // TODO dispatch event.
            return true;
        });

        // Set data.
        vh.tvBalanceName.setText(balance.name);
        Utils.setAmountTextAndColorWithLimits(vh.tvBalanceAmount, balance.getTotalBalance(), balance.yellowLimit,
                balance.redLimit);
    }

    static class BalanceVH extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        ViewGroup content;
        @BindView(R.id.balance_name)
        TextView tvBalanceName;
        @BindView(R.id.balance_amount)
        TextView tvBalanceAmount;

        BalanceVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
