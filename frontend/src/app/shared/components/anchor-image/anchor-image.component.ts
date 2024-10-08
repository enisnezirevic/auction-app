import {CommonModule, NgOptimizedImage} from "@angular/common";
import {Component, Input} from "@angular/core";
import {RouterLink} from "@angular/router";

@Component({
  selector: "app-anchor-image",
  standalone: true,
  imports: [CommonModule, NgOptimizedImage, RouterLink],
  templateUrl: "./anchor-image.component.html",
  styleUrls: ["./anchor-image.component.scss"]
})
export class AnchorImageComponent {
  @Input({required: true}) link!: string;
  @Input({required: true}) imageSource!: string;
  @Input({required: true}) alt!: string;
  @Input({required: true}) externalLink!: boolean;
  @Input() imageWidth: number = 24;
  @Input() imageHeight: number = 24;
}
