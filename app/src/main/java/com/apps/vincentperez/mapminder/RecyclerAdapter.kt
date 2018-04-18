import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.apps.vincentperez.mapminder.R
import com.google.android.gms.maps.model.Marker


class RecyclerAdapter (private val markers : ArrayList<Marker>): RecyclerView.Adapter<RecyclerAdapter.ReminderHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ReminderHolder{
        val inflatedView = parent.inflate(R.layout.near_list_item_row, false)
        return ReminderHolder(inflatedView)
    }

    override fun getItemCount() = markers.size

    override fun onBindViewHolder(holder:RecyclerAdapter.ReminderHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class ReminderHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        //2
        private var view: View = v
        //private var photo: ? = null

        //3
        init {
            v.setOnClickListener(this)
        }

        //4
        override fun onClick(v: View) {
            val context = itemView.context

            //val showPhotoIntent = Intent(context, PhotoActivity::class.java)
            //showPhotoIntent.putExtra(REMINDER_KEY, photo)
            //context.startActivity(showPhotoIntent)
        }

        companion object {
            //5
            private val REMINDER_KEY = "REMINDER"
        }
    }
}