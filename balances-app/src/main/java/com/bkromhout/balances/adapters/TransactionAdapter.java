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
import com.bkromhout.balances.data.DateUtils;
import com.bkromhout.balances.data.models.Transaction;
import com.bkromhout.rrvl.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Adapter class which handles binding {@link Transaction}s to a RealmRecyclerView.
 */
public class TransactionAdapter extends RealmRecyclerViewAdapter<Transaction, RecyclerView.ViewHolder> {
    /**
     * Whether or not the adapter should consider itself to be in selection mode.
     */
    private boolean inSelectionMode = false;

    /**
     * Create a new {@link TransactionAdapter}.
     * @param context      Context.
     * @param realmResults Results of a Realm query to display.
     */
    public TransactionAdapter(Context context, RealmResults<Transaction> realmResults) {
        super(context, realmResults);
        setHasStableIds(true);
    }

    /**
     * Set whether or not the adapter should consider itself to be in selection mode. This is necessary to determine
     * what to do when an item is tapped.
     * @param enabled Whether selection mode should be enabled or not.
     */
    public void setSelectionMode(boolean enabled) {
        this.inSelectionMode = enabled;
    }

    @Override
    public long getItemId(int position) {
        final Transaction transaction = realmResults.get(position);
        return transaction.isValid() ? transaction.uniqueId : -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TransactionVH(inflater.inflate(R.layout.transaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TransactionVH vh = (TransactionVH) holder;
        final Transaction transaction = realmResults.get(position);
        if (!transaction.isValid()) return;

        // Visually distinguish selected cards during multi-select mode.
        vh.content.setActivated(isSelected(position));

        // Set click handler.
        vh.content.setOnClickListener(view -> {
            if (inSelectionMode) {
                toggleSelected(holder.getAdapterPosition());
                return;
            }

            // TODO Send event to open the transaction detail activity.
        });

        // Set long click handler.
        vh.content.setOnLongClickListener(view -> {
            // TODO dispatch event.
            return true;
        });

        // Set data.
        vh.tvTransName.setText(transaction.name);
        Utils.setAmountTextAndColor(vh.tvTransAmount, transaction.amount, true);
        vh.tvTransCat.setText(transaction.category.name);
        vh.tvTransTimestamp.setText(DateUtils.parseDateToString(transaction.timestamp));
    }

    static class TransactionVH extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        ViewGroup content;
        @BindView(R.id.trans_name)
        TextView tvTransName;
        @BindView(R.id.trans_amount)
        TextView tvTransAmount;
        @BindView(R.id.trans_cat)
        TextView tvTransCat;
        @BindView(R.id.trans_timestamp)
        TextView tvTransTimestamp;

        TransactionVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
