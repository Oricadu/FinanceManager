package com.oricadu.financemanager.ui.aims;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.oricadu.financemanager.R;
import com.oricadu.financemanager.model.Aim;
import com.oricadu.financemanager.model.Category;
import com.oricadu.financemanager.ui.MyDialogFragment;
import com.oricadu.financemanager.ui.categories.CategoriesFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AimsFragment extends Fragment {

    private AimsViewModel aimsViewModel;

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference reference = database.getReference();
    private static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static FirebaseUser user = auth.getCurrentUser();

    private RecyclerView recyclerView;
    private FloatingActionButton button;

    private Button allocate;
    private EditText allocateSum;

    public static class AimAddDialog extends DialogFragment {

        EditText inputName, inputSum;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_fragment, null);
            ViewGroup linearLayoutDialog = dialogView.findViewById(R.id.linear_layout_dialog);
            inputName = (EditText) dialogView.findViewById(R.id.aim_name);
            inputSum = (EditText) dialogView.findViewById(R.id.aim_sum);


            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                    .setTitle("Add new aim")
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onDismiss(dialog);
                        }
                    })
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String aimName = inputName.getText().toString().trim();
                            int aimSum = Integer.parseInt(inputSum.getText().toString().trim());
                            reference.child(user.getUid())
                                    .child("Aims")
                                    .child(aimName)
                                    .setValue(new Aim(aimName,
                                            aimSum,
                                            0, 0));
                        }
                    });
            dialogBuilder.setView(dialogView);
//                                    .setNeutralButton(R.string.maybe, this)
                    /*.setMessage("message_text")*/;



            return dialogBuilder.create();

        }
    }

    protected static class AimViewHolder extends RecyclerView.ViewHolder {
        TextView aimName;
        TextView aimSum;
        TextView aimAccumulatedSum;
        TextView aimPercent;
        TextView aimAllocatePercent;

        public AimViewHolder(@NonNull View itemView) {
            super(itemView);

            aimName = itemView.findViewById(R.id.aim_name);
            aimSum = itemView.findViewById(R.id.aim_sum);
            aimAccumulatedSum = itemView.findViewById(R.id.aim_accumulated_sum);
            aimPercent = itemView.findViewById(R.id.aim_percent);
            aimAllocatePercent = itemView.findViewById(R.id.aim_allocate_percent);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                }
            });
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        aimsViewModel =
                new ViewModelProvider(this).get(AimsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_aims, container, false);
        /*final TextView textView = root.findViewById(R.id.text_categories);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

//        auth = FirebaseAuth.getInstance();
//        reference = database.getReference();
//        user = auth.getCurrentUser();

        if (user != null) {
            Log.i("User", "user.uid=" + user.getUid());

        }

        button = root.findViewById(R.id.action_button2);
        allocate = root.findViewById(R.id.allocate);
        allocateSum = root.findViewById(R.id.aim_allocate_sum);



        recyclerView = (RecyclerView) root.findViewById(R.id.aim_recycler);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        if (user != null) {


            final FirebaseRecyclerAdapter<Aim, AimsFragment.AimViewHolder> adapter;
            adapter = new FirebaseRecyclerAdapter<Aim, AimsFragment.AimViewHolder>(
                    Aim.class,
                    R.layout.aim_item,
                    AimsFragment.AimViewHolder.class,
                    reference.child(user.getUid())
                            .child("Aims")) {
                @Override
                protected void populateViewHolder(AimsFragment.AimViewHolder aimsViewHolder, Aim aim, int i) {
                    String percent = new DecimalFormat("##.#%").format((double) aim.getAimAccumulatedSum() / aim.getAimSum());
                    Log.i("aim", "inside adapter user.uid=" + user.getUid());

                    aimsViewHolder.aimName.setText(aim.getAimName());
                    aimsViewHolder.aimSum.setText(String.valueOf(aim.getAimSum()));
                    aimsViewHolder.aimAccumulatedSum.setText(String.valueOf(aim.getAimAccumulatedSum()));
                    aimsViewHolder.aimPercent.setText(percent);
                    aimsViewHolder.aimAllocatePercent.setText(String.valueOf(aim.getAimPercent()));
                    Log.i("aim", aim.getAimAccumulatedSum() + " sum " + aim.getAimSum());
                }

            };

            recyclerView.setAdapter(adapter);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AimAddDialog dialog = new AimAddDialog();
                    dialog.show(getChildFragmentManager(), "tag");
                }
            });

            allocate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int sum = Integer.parseInt(allocateSum.getText().toString().trim());
                    reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataValues : snapshot.child("Aims").getChildren()) {
                                Aim aim = dataValues.getValue(Aim.class);
                                double percent = (double) aim.getAimPercent() / 100;
                                double accumulatedSum = aim.getAimAccumulatedSum();
                                double newSum = (double) accumulatedSum + sum * percent;
                                dataValues.getRef().child("aimAccumulatedSum").setValue(newSum);

                                Log.i("aim", "aim " + aim);
                                Log.i("aim", "percent " + percent);
                                Log.i("aim", "oldSum " + accumulatedSum);
                                Log.i("aim", "newSum " + newSum);
                                Log.i("aim", "snapshot " + dataValues.getRef().child("aimAccumulatedSum"));
                                Log.i("aim", "key " + dataValues.getKey());


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    DatabaseReference ref = adapter.getRef(position);
                    ref.removeValue();
                }
            };

            new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);



        }





        return root;
    }

}