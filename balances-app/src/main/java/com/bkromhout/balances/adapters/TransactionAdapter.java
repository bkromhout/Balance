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
import com.bkromhout.balances.events.TransactionClickEvent;
import com.bkromhout.rrvl.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Adapter class which handles binding {@link Transaction}s to a RealmRecyclerView.
 */
public class TransactionAdapter extends RealmRecyclerViewAdapter<Transaction, RecyclerView.ViewHolder> {
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
     * Get the UIDs of the selected items.
     * @return Long array containing UIDs of selected items.
     */
    public Long[] getSelectedItemUids() {
        ArrayList<Long> uids = new ArrayList<>(selectedPositions.size());
        for (int selectedPos : selectedPositions) {
            uids.add((Long) ids.get(selectedPos));
        }
        return uids.toArray(new Long[uids.size()]);
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

        // Visually distinguish selected items during multi-select mode.
        vh.content.setSelected(isSelected(position));

        // Set click handler.
        vh.content.setOnClickListener(view -> EventBus.getDefault().post(new TransactionClickEvent(
                TransactionClickEvent.Type.NORMAL, transaction.uniqueId, vh.getAdapterPosition(),
                vh.getLayoutPosition())));

        // Set long click handler.
        vh.content.setOnLongClickListener(view -> {
            EventBus.getDefault().post(new TransactionClickEvent(TransactionClickEvent.Type.LONG, transaction.uniqueId,
                    vh.getAdapterPosition(), vh.getLayoutPosition()));
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
