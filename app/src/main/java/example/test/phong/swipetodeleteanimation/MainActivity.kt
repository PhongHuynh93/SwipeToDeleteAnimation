package example.test.phong.swipetodeleteanimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rcv?.apply {
            adapter = TestAdapter()
            layoutManager = LinearLayoutManager(this@MainActivity)
            val itemTouchHelper = ItemTouchHelper(TestItemTouchHelper(this@MainActivity))
            itemTouchHelper.attachToRecyclerView(this)
        }
    }
}

class TestAdapter : RecyclerView.Adapter<VH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false))
    }

    override fun getItemCount(): Int {
        return 20
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
    }

}

class VH(itemView: View): RecyclerView.ViewHolder(itemView) {

}

