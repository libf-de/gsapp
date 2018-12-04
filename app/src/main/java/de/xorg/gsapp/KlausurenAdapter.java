package de.xorg.gsapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class KlausurenAdapter extends RecyclerView.Adapter<KlausurenAdapter.ViewHolder> {

    private List<Klausur> klausurs;
    private LayoutInflater layout;
    private Context mContext;


    public KlausurenAdapter(Context c, List<Klausur>l){
        this.klausurs = l;
        layout = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = c;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = layout.inflate(R.layout.list_item,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fachKuerzel = klausurs.get(position).getFachShort();
        holder.iconView.setText(klausurs.get(position).getTitle().toUpperCase());
        holder.iconView.setSolidColor(Color.parseColor(Util.getFachColor(fachKuerzel)));
        holder.titleView.setText(Util.LongName(fachKuerzel));
        holder.timeView.setText(klausurs.get(position).getRemainingTime());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Recycle Click", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.klausurs.size();
    }

    /*public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }*/

    /*private BitmapDrawable writeText(int color, String text) {
        Bitmap b2m = BitmapFactory.decodeResource(mContext.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(mContext, 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(mContext, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return new BitmapDrawable(mContext.getResources(), bm);
    }*/


    /*@Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = layout.inflate(R.layout.list_item,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return gasList.size();
    }*/


    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView titleView, timeView;
        protected CircularTextView iconView;
        //protected ImageView iconView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.titleView = itemView.findViewById(R.id.title);
            this.timeView = itemView.findViewById(R.id.timespan);
            this.iconView = itemView.findViewById(R.id.iconView);
        }


    }

    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int mVerticalSpaceHeight;

        public DividerItemDecoration(int mVerticalSpaceHeight) {
            this.mVerticalSpaceHeight = mVerticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = mVerticalSpaceHeight;
            //outRect.left = mVerticalSpaceHeight;
            //outRect.right = mVerticalSpaceHeight;
        }
    }


}