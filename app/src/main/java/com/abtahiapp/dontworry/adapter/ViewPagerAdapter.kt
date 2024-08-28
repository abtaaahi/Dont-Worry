package com.abtahiapp.dontworry.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.abtahiapp.dontworry.fragment.HomeFragment
import com.abtahiapp.dontworry.fragment.MovieFragment
import com.abtahiapp.dontworry.fragment.MusicFragment
import com.abtahiapp.dontworry.fragment.VideoFragment
import com.abtahiapp.dontworry.fragment.ArticleFragment
import com.abtahiapp.dontworry.fragment.WeatherQuoteFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragmentList = listOf(
        HomeFragment(),
        MovieFragment(),
        MusicFragment(),
        VideoFragment(),
        ArticleFragment(),
        WeatherQuoteFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}
