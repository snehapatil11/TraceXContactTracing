package com.example.tracexcontacttracing.fragment.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChartTabsAdapterV2(fragment: Fragment): FragmentStateAdapter(fragment) {

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun createFragment(position: Int): Fragment {
        return mFragmentList.get(position)
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    fun getTitle(position: Int): String {
       return mFragmentTitleList.get(position)
    }
}