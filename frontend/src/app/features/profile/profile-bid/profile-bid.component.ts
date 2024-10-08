import {CommonModule} from "@angular/common";
import {Component, OnInit} from "@angular/core";
import {RouterLink} from "@angular/router";
import {Observable} from "rxjs";
import {LoaderComponent} from "../../../shared/components/loader/loader.component";
import {Api} from "../../../shared/services/api.service";
import {BidService} from "../../../shared/services/user/bid.service";
import {LoaderService} from "../../../shared/services/loader.service";
import {ShopRouteEndpoint} from "../../shop/shop-routes";
import {TableDataRow} from "../components/table-data-row/table-data-row.component";
import {TableEmptyComponent} from "../components/table-empty/table-empty.component";
import {TableComponent, TableSection} from "../components/table/table.component";
import {ProfileRouteEndpoint} from "../profile-routes";
import {ProfileSharedNavTabComponent} from "../shared-nav/profile-shared-nav-tab.component";
import UserBiddingInfo = Api.BidApi.UserBiddingInfo;

@Component({
  selector: "profile-bid",
  standalone: true,
  imports: [CommonModule, TableComponent, TableEmptyComponent, TableDataRow, RouterLink, LoaderComponent, ProfileSharedNavTabComponent],
  templateUrl: "./profile-bid.component.html",
  styleUrls: ["./profile-bid.component.scss"]
})
export class ProfileBidComponent implements OnInit {
  bids$: Observable<Array<UserBiddingInfo>> | undefined;
  protected readonly ProfileRouteEndpoint = ProfileRouteEndpoint;
  protected readonly ShopRouteEndpoint = ShopRouteEndpoint;

  constructor(protected biddingService: BidService, protected loader: LoaderService) {
  }

  public ngOnInit(): void {
    this.bids$ = this.biddingService.getAllUserBiddingInfo();
  }

    protected readonly TableSection = TableSection;
}
