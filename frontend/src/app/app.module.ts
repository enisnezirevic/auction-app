import {HttpClientModule} from "@angular/common/http";
import {NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppRoutingModule} from "./app-routing.module";
import {AppComponent} from "./app.component";
import {AboutUsPage} from "./features/home/home-about-us/about-us-page.component";
import {PrivacyPolicyPage} from "./features/home/home-privacy-policy/privacy-policy-page.component";
import {TermsConditionsPage} from "./features/home/home-terms-conditions/terms-conditions-page.component";
import {CornerMessageComponent} from "./shared/components/corner-message/corner-message.component";
import {FooterComponent} from "./shared/components/footer/footer.component";
import {LoaderComponent} from "./shared/components/loader/loader.component";
import {NavbarComponent} from "./shared/components/navbar/navbar.component";

@NgModule({
  declarations: [
    AppComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        HttpClientModule,
        BrowserAnimationsModule,

        NavbarComponent,
        FooterComponent,

        AboutUsPage,
        PrivacyPolicyPage,
        TermsConditionsPage,
        LoaderComponent,
        CornerMessageComponent
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
